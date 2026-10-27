package co.com.bookhive.circulation.domain.user.finduser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindUserDomainTest {

	private static final UUID USER = UUIDHelper.generateNew();
	private static final String NAME = "Ana Gómez";
	private static final String EMAIL = "ana.gomez@uco.edu.co";
	private static final int LOAN_DAYS = 15;
	private static final int MAX_LOANS = 3;

	private FakeRepositories.Users users;
	private FakeRepositories.Fines fines;
	private FakeRepositories.Loans loans;
	private FakeRepositories.Parameters parameters;
	private FakeRepositories.Reservations reservations;
	private FakeRepositories.BookCopies bookCopies;

	@BeforeEach
	void prepare() {
		users = new FakeRepositories.Users().with(FakeRepositories.user(USER, NAME, EMAIL));
		fines = new FakeRepositories.Fines();
		loans = new FakeRepositories.Loans();
		parameters = new FakeRepositories.Parameters().with(LOAN_DAYS, MAX_LOANS);
		reservations = new FakeRepositories.Reservations();
		bookCopies = new FakeRepositories.BookCopies();
	}

	@Test
	@DisplayName("entrega los datos del usuario y sus préstamos; sin multas ni tope no tiene bloqueos")
	void findsUserWithoutBlocks() {
		loans.withActiveLoans(1);

		final FoundUserDomain found = search(" Ana.Gomez@UCO.edu.co ").find();

		assertEquals(USER, found.id());
		assertEquals(NAME, found.name());
		assertEquals(EMAIL, found.email());
		assertEquals(1, found.activeLoans());
		assertEquals(MAX_LOANS, found.maxLoans());
		assertEquals(0, found.pendingFines());
		assertTrue(found.blocks().isEmpty());
		assertTrue(found.readyReservations().isEmpty());
	}

	@Test
	@DisplayName("lista las reservas con ejemplar apartado, con su código y la que vence primero arriba")
	void listsReadyReservations() {
		final ReservationEntity later = assignedReservation(USER, "BH-0002", LocalDate.of(2026, 10, 30));
		final ReservationEntity sooner = assignedReservation(USER, "BH-0001", LocalDate.of(2026, 10, 28));
		assignedReservation(UUIDHelper.generateNew(), "BH-0003", LocalDate.of(2026, 10, 27));
		reservations.with(FakeRepositories.reservation(
				UUIDHelper.generateNew(), USER, ReservationEntity.STATUS_WAITING, Instant.now()));

		final List<ReadyReservationDomain> ready = search(EMAIL).find().readyReservations();

		assertEquals(List.of(
				new ReadyReservationDomain(sooner.getId(), sooner.getBook(), sooner.getBookCopy(), "BH-0001",
						sooner.getPickupDeadline()),
				new ReadyReservationDomain(later.getId(), later.getBook(), later.getBookCopy(), "BH-0002",
						later.getPickupDeadline())),
				ready);
	}

	@Test
	@DisplayName("si el ejemplar apartado ya no existe, la reserva sale sin código")
	void readyReservationWithoutCopyHasNoBarcode() {
		final ReservationEntity reservation = FakeRepositories.reservation(
				UUIDHelper.generateNew(), USER, ReservationEntity.STATUS_ASSIGNED, Instant.now());
		reservation.setBookCopy(UUIDHelper.generateNew());
		reservations.with(reservation);

		assertEquals("", search(EMAIL).find().readyReservations().get(0).barcode());
	}

	@Test
	@DisplayName("con multas pendientes queda bloqueado por multas")
	void blocksUserWithPendingFines() {
		fines.withPendingFine();

		final FoundUserDomain found = search(EMAIL).find();

		assertEquals(1, found.pendingFines());
		assertEquals(List.of(MessageKey.USER_WITH_PENDING_FINES), found.blocks());
	}

	@Test
	@DisplayName("con el máximo de préstamos activos queda bloqueado por tope")
	void blocksUserAtLoanLimit() {
		loans.withActiveLoans(MAX_LOANS);

		final FoundUserDomain found = search(EMAIL).find();

		assertEquals(List.of(MessageKey.USER_AT_LOAN_LIMIT), found.blocks());
	}

	@Test
	@DisplayName("con multas y en el tope muestra los dos bloqueos")
	void listsEveryBlock() {
		fines.withPendingFine();
		loans.withActiveLoans(MAX_LOANS);

		final FoundUserDomain found = search(EMAIL).find();

		assertEquals(List.of(MessageKey.USER_WITH_PENDING_FINES, MessageKey.USER_AT_LOAN_LIMIT), found.blocks());
	}

	@Test
	@DisplayName("un usuario suspendido queda bloqueado por la suspensión")
	void blocksSuspendedUser() {
		storeUserWithStatus(UserEntity.STATUS_SUSPENDED);

		assertEquals(List.of(MessageKey.USER_SUSPENDED), search(EMAIL).find().blocks());
	}

	@Test
	@DisplayName("un usuario dado de baja queda bloqueado por la baja")
	void blocksRetiredUser() {
		storeUserWithStatus(UserEntity.STATUS_RETIRED);

		assertEquals(List.of(MessageKey.USER_RETIRED), search(EMAIL).find().blocks());
	}

	@Test
	@DisplayName("un usuario suspendido, con multas y en el tope muestra la suspensión primero")
	void listsStatusBlockFirst() {
		storeUserWithStatus(UserEntity.STATUS_SUSPENDED);
		fines.withPendingFine();
		loans.withActiveLoans(MAX_LOANS);

		assertEquals(
				List.of(MessageKey.USER_SUSPENDED, MessageKey.USER_WITH_PENDING_FINES, MessageKey.USER_AT_LOAN_LIMIT),
				search(EMAIL).find().blocks());
	}

	@Test
	@DisplayName("bloquea por suspensión igual que lo rechaza el registro del préstamo")
	void blocksSuspendedLikeLending() {
		storeUserWithStatus(UserEntity.STATUS_SUSPENDED);

		assertEquals(rejectionOfLending(), search(EMAIL).find().blocks().get(0));
	}

	@Test
	@DisplayName("bloquea por baja igual que lo rechaza el registro del préstamo")
	void blocksRetiredLikeLending() {
		storeUserWithStatus(UserEntity.STATUS_RETIRED);

		assertEquals(rejectionOfLending(), search(EMAIL).find().blocks().get(0));
	}

	@Test
	@DisplayName("bloquea por multas igual que lo rechaza el registro del préstamo")
	void blocksPendingFinesLikeLending() {
		fines.withPendingFine();

		assertEquals(rejectionOfLending(), search(EMAIL).find().blocks().get(0));
	}

	@Test
	@DisplayName("bloquea por tope igual que lo rechaza el registro del préstamo")
	void blocksLoanLimitLikeLending() {
		loans.withActiveLoans(MAX_LOANS);

		assertEquals(rejectionOfLending(), search(EMAIL).find().blocks().get(0));
	}

	@Test
	@DisplayName("sin correo pide que se indique")
	void requiresEmail() {
		final FindUserDomain search = search("   ");

		final BookHiveException exception = assertThrows(BookHiveException.class, search::validate);

		assertEquals(MessageKey.USER_EMAIL_REQUIRED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.MISSING_DATA, exception.getKind());
	}

	@Test
	@DisplayName("con correo la validación pasa")
	void acceptsEmail() {
		assertDoesNotThrow(search(EMAIL)::validate);
	}

	@Test
	@DisplayName("si nadie tiene ese correo avisa que no existe")
	void rejectsUnknownEmail() {
		final FindUserDomain search = search("otra.persona@uco.edu.co");

		final BookHiveException exception = assertThrows(BookHiveException.class, search::find);

		assertEquals(MessageKey.USER_NOT_FOUND, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
	}

	@Test
	@DisplayName("un correo más largo de lo que admite la base no se consulta: nadie puede tenerlo")
	void rejectsEmailLongerThanColumn() {
		final FindUserDomain search = search("a".repeat(FindUserDomain.MAX_EMAIL_LENGTH) + "@uco.edu.co");

		final BookHiveException exception = assertThrows(BookHiveException.class, search::find);

		assertEquals(MessageKey.USER_NOT_FOUND, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("si dos correos solo se diferencian en mayúsculas prefiere el escrito igual")
	void prefersEmailWrittenTheSame() {
		final UUID other = UUIDHelper.generateNew();
		users.with(FakeRepositories.user(other, "Ana Gómez Ruiz", "Ana.Gomez@uco.edu.co"));

		assertEquals(other, search("Ana.Gomez@uco.edu.co").find().id());
		assertEquals(USER, search(EMAIL).find().id());
	}

	@Test
	@DisplayName("si dos correos coinciden y ninguno está escrito igual no elige uno al azar")
	void rejectsAmbiguousEmail() {
		users.with(FakeRepositories.user(UUIDHelper.generateNew(), "Ana Gómez Ruiz", "Ana.Gomez@uco.edu.co"));
		final FindUserDomain search = search("ANA.GOMEZ@UCO.EDU.CO");

		final BookHiveException exception = assertThrows(BookHiveException.class, search::find);

		assertEquals(MessageKey.USER_NOT_FOUND, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("si la institución no tiene reglas de préstamo no inventa un tope")
	void rejectsInstitutionWithoutParameters() {
		parameters = new FakeRepositories.Parameters();
		final FindUserDomain search = search(EMAIL);

		final BookHiveException exception = assertThrows(BookHiveException.class, search::find);

		assertEquals(MessageKey.INSTITUTION_WITHOUT_PARAMETERS, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
	}

	@Test
	@DisplayName("sin repositorios no puede buscar")
	void requiresRepositories() {
		final FindUserDomain search = new FindUserDomain(EMAIL);
		final FindUserDomain withoutReservations = new FindUserDomain(EMAIL)
				.withRepositories(users, fines, loans, parameters);

		final BookHiveException exception = assertThrows(BookHiveException.class, search::find);

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, withoutReservations::find).getKind());
	}

	private FindUserDomain search(final String email) {
		return new FindUserDomain(email)
				.withRepositories(users, fines, loans, parameters)
				.withReservations(reservations, bookCopies);
	}

	private ReservationEntity assignedReservation(final UUID user, final String barcode, final LocalDate deadline) {
		final BookCopyEntity copy = FakeRepositories.bookCopy(UUIDHelper.generateNew(), BookCopyEntity.STATUS_RESERVED, true);
		copy.setBarcode(barcode);
		bookCopies.with(copy);

		final ReservationEntity reservation = FakeRepositories.reservation(
				UUIDHelper.generateNew(), user, ReservationEntity.STATUS_ASSIGNED, Instant.now());
		reservation.setBookCopy(copy.getId());
		reservation.setPickupDeadline(deadline);
		reservations.with(reservation);
		return reservation;
	}

	private String rejectionOfLending() {
		final UUID bookCopy = UUIDHelper.generateNew();
		final FakeRepositories.BookCopies heldCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopy(bookCopy, BookCopyEntity.STATUS_RESERVED, true));
		final ReservationEntity held = FakeRepositories.reservation(
				UUIDHelper.generateNew(), USER, ReservationEntity.STATUS_ASSIGNED, Instant.now());
		held.setBookCopy(bookCopy);

		final LendBookDomain loan = new LendBookDomain(
				UUIDHelper.getDefaultValue(),
				bookCopy,
				USER,
				UUIDHelper.generateNew(),
				UUIDHelper.generateNew(),
				CurrentInstitution.today(CurrentInstitution.parameters(parameters)),
				DateHelper.getDefaultValue())
				.withRepositories(users, heldCopies, fines, loans, parameters)
				.withReservations(new FakeRepositories.Reservations().with(held));

		return assertThrows(BookHiveException.class, loan::validate).getUserMessageKey();
	}

	private void storeUserWithStatus(final String status) {
		final UserEntity user = FakeRepositories.user(USER, NAME, EMAIL);
		user.setStatus(status);
		users = new FakeRepositories.Users().with(user);
	}

}
