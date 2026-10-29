package co.com.bookhive.circulation.services.usecase.loan.lendbook.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.impl.mapper.LoanDomainMapperImpl;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class LendBookUseCaseImplTest {

	private static final LocalDate TODAY = DateHelper.todayIn(ZoneId.of(InstitutionParametersEntity.DEFAULT_TIME_ZONE));

	private static final UUID BOOK_COPY = UUIDHelper.generateNew();
	private static final UUID USER = UUIDHelper.generateNew();
	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID LIBRARIAN = UUIDHelper.generateNew();
	private static final int LOAN_DAYS = 15;

	private FakeRepositories.Users users;
	private FakeRepositories.BookCopies bookCopies;
	private FakeRepositories.Fines fines;
	private FakeRepositories.Loans loans;
	private FakeRepositories.Parameters parameters;
	private FakeRepositories.Reservations reservations;
	private List<String> audited;
	private List<NotificationRequestEntity> notices;

	@BeforeEach
	void prepare() {
		users = new FakeRepositories.Users().with(FakeRepositories.user(USER, "Ana Gómez", "ana.gomez@uco.edu.co"));
		reserveCopyFor(USER);
		fines = new FakeRepositories.Fines();
		loans = new FakeRepositories.Loans();
		parameters = new FakeRepositories.Parameters().with(LOAN_DAYS, 3);
		audited = new ArrayList<>();
		notices = new ArrayList<>();
	}

	@Test
	@DisplayName("un ejemplar disponible sin reserva no se presta y no se guarda nada")
	void availableCopyWithoutReservationIsNotLent() {
		bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_AVAILABLE, true));
		reservations = new FakeRepositories.Reservations();
		final LendBookUseCaseImpl useCase = useCase();
		final LendBookDomain request = request();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.BOOK_COPY_NOT_RESERVED_FOR_USER, exception.getUserMessageKey());
		assertTrue(bookCopies.statusChanges().isEmpty());
		assertTrue(loans.created().isEmpty());
		assertTrue(notices.isEmpty());
	}

	@Test
	@DisplayName("completa la institución y el bibliotecario con los datos de la sesión")
	void takesInstitutionAndLibrarianFromSession() {
		final LendBookDomain registered = useCase().execute(request());

		assertEquals(INSTITUTION, registered.getInstitution());
		assertEquals(LIBRARIAN, registered.getLibrarian());
	}

	@Test
	@DisplayName("deja constancia del préstamo registrado con la institución y quien lo atendió")
	void auditsRegisteredLoan() {
		final LendBookDomain registered = useCase().execute(request());

		assertEquals(List.of(AuditPort.LOAN_REGISTERED + " " + INSTITUTION + " " + LIBRARIAN + " " + registered.getId()),
				audited);
	}

	@Test
	@DisplayName("registra el préstamo con identificador nuevo, fecha de entrega y lo guarda")
	void registersLoan() {
		final LendBookDomain registered = useCase().execute(request());

		assertFalse(UUIDHelper.isDefaultValue(registered.getId()));
		assertEquals(7, registered.getId().version());
		assertEquals(TODAY, registered.getLoanDate());
		assertEquals(TODAY.plusDays(LOAN_DAYS), registered.getDueDate());

		assertEquals(1, loans.created().size());
		final LoanEntity saved = loans.created().get(0);
		assertEquals(registered.getId(), saved.getId());
		assertEquals(BOOK_COPY, saved.getBookCopy().getId());
		assertEquals(USER, saved.getUser().getId());
		assertEquals(LoanEntity.STATUS_ACTIVE, saved.getStatus());
	}

	@Test
	@DisplayName("marca el ejemplar como prestado solo si sigue apartado, en una sola instrucción")
	void marksBookCopyAsLent() {
		useCase().execute(request());

		assertEquals(List.of(BOOK_COPY + ":RESERVADO->PRESTADO"), bookCopies.statusChanges());
		assertTrue(bookCopies.updated().isEmpty());
	}

	@Test
	@DisplayName("si otro préstamo del mismo ejemplar ganó al mismo tiempo, se rechaza sin guardar nada")
	void simultaneousLoanIsRejected() {
		bookCopies.changedByOtherRequest();
		final LendBookUseCaseImpl useCase = useCase();
		final LendBookDomain request = request();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.BOOK_COPY_NOT_AVAILABLE, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertTrue(loans.created().isEmpty());
		assertTrue(audited.isEmpty());
		assertTrue(notices.isEmpty());
	}

	@Test
	@DisplayName("no guarda nada si el préstamo se rechaza")
	void doesNotSaveRejectedLoan() {
		fines = new FakeRepositories.Fines().withPendingFine();
		final LendBookUseCaseImpl useCase = useCase();
		final LendBookDomain request = request();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.USER_WITH_PENDING_FINES, exception.getUserMessageKey());
		assertTrue(loans.created().isEmpty());
		assertTrue(bookCopies.statusChanges().isEmpty());
		assertTrue(audited.isEmpty());
	}

	@Test
	@DisplayName("pide el correo de préstamo registrado con el usuario, el libro y la fecha de entrega")
	void requestsLoanRegisteredNotice() {
		final UUID book = UUIDHelper.generateNew();
		final BookCopyEntity copy = FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_RESERVED, true);
		copy.setBook(book);
		bookCopies = new FakeRepositories.BookCopies().with(copy);

		final LendBookDomain registered = useCase().execute(request());

		assertEquals(List.of(new NotificationRequestEntity(
				LendBookDomain.NOTICE_KIND, USER, book, registered.getId(), registered.getDueDate())), notices);
	}

	@Test
	@DisplayName("no pide el aviso si el préstamo se rechaza")
	void doesNotRequestNoticeWhenRejected() {
		fines.withPendingFine();
		final LendBookUseCaseImpl useCase = useCase();
		final LendBookDomain request = request();

		assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(List.of(), notices);
	}

	@Test
	@DisplayName("rechaza el préstamo si no llegan datos")
	void rejectsNullDomain() {
		final LendBookUseCaseImpl useCase = useCase();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(MessageKey.LOAN_DATA_REQUIRED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("no deja crear el caso de uso si falta una dependencia")
	void requiresDependencies() {
		final FakeSession session = new FakeSession();
		final AuditPort audit = this::audit;

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> new LendBookUseCaseImpl(parameters, bookCopies, fines, loans, users, null, session, audit,
						notices::add, reservations));
		final LoanDomainMapperImpl mapper = new LoanDomainMapperImpl();

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.LOAN_UNEXPECTED, exception.getUserMessageKey());
		assertThrows(BookHiveException.class, () -> new LendBookUseCaseImpl(
				parameters, bookCopies, fines, loans, users, mapper, session, audit, notices::add, null));
	}

	@Test
	@DisplayName("el ejemplar apartado se le presta a quien lo reservó y su reserva queda retirada")
	void holderPicksUpReservedCopy() {
		final ReservationEntity held = reserveCopyFor(USER);

		final LendBookDomain registered = useCase().execute(request());

		assertFalse(UUIDHelper.isDefaultValue(registered.getId()));
		assertEquals(List.of(BOOK_COPY + ":RESERVADO->PRESTADO"), bookCopies.statusChanges());
		assertEquals(ReservationEntity.STATUS_PICKED_UP, held.getStatus());
		assertEquals(1, loans.created().size());
	}

	@Test
	@DisplayName("el ejemplar apartado para otra persona no se presta")
	void reservedCopyIsNotLentToSomeoneElse() {
		final ReservationEntity held = reserveCopyFor(UUIDHelper.generateNew());
		final LendBookUseCaseImpl useCase = useCase();
		final LendBookDomain request = request();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.BOOK_COPY_RESERVED, exception.getUserMessageKey());
		assertEquals(ReservationEntity.STATUS_ASSIGNED, held.getStatus());
		assertTrue(bookCopies.statusChanges().isEmpty());
		assertTrue(loans.created().isEmpty());
	}

	@Test
	@DisplayName("si la reserva venció mientras se prestaba, el préstamo se rechaza y el ejemplar no cambia")
	void reservationChangedWhileLending() {
		reserveCopyFor(USER);
		reservations.changedByOtherRequestTo(ReservationEntity.STATUS_EXPIRED);
		final LendBookUseCaseImpl useCase = useCase();
		final LendBookDomain request = request();

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.BOOK_COPY_RESERVED, exception.getUserMessageKey());
		assertTrue(bookCopies.statusChanges().isEmpty());
		assertTrue(loans.created().isEmpty());
	}

	private LendBookUseCaseImpl useCase() {
		return new LendBookUseCaseImpl(parameters, bookCopies, fines, loans, users, new LoanDomainMapperImpl(),
				new FakeSession(), this::audit, notices::add, reservations);
	}

	private void audit(final String action, final UUID institution, final UUID person, final String detail) {
		audited.add(action + " " + institution + " " + person + " " + detail);
	}

	private static LendBookDomain request() {
		return new LendBookDomain(null, BOOK_COPY, USER, null, null, null, null);
	}

	private ReservationEntity reserveCopyFor(final UUID holder) {
		bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_RESERVED, true));
		final ReservationEntity held = FakeRepositories.reservation(
				UUIDHelper.generateNew(), holder, ReservationEntity.STATUS_ASSIGNED, Instant.now());
		held.setBookCopy(BOOK_COPY);
		reservations = new FakeRepositories.Reservations().with(held);
		return held;
	}

	private static final class FakeSession implements SessionContextPort {

		@Override
		public UUID getInstitution() {
			return INSTITUTION;
		}

		@Override
		public UUID getLibrarian() {
			return LIBRARIAN;
		}

	}

}
