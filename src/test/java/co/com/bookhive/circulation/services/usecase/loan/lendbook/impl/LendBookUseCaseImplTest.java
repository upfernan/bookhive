package co.com.bookhive.circulation.services.usecase.loan.lendbook.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.impl.mapper.LoanDomainMapperImpl;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class LendBookUseCaseImplTest {

	private static final UUID BOOK_COPY = UUIDHelper.generateNew();
	private static final UUID USER = UUIDHelper.generateNew();
	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID LIBRARIAN = UUIDHelper.generateNew();
	private static final int LOAN_DAYS = 15;

	private FakeRepositories.BookCopies bookCopies;
	private FakeRepositories.Fines fines;
	private FakeRepositories.Loans loans;
	private FakeRepositories.Parameters parameters;
	private List<String> audited;

	@BeforeEach
	void prepare() {
		bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_AVAILABLE, true));
		fines = new FakeRepositories.Fines();
		loans = new FakeRepositories.Loans();
		parameters = new FakeRepositories.Parameters().with(LOAN_DAYS, 3);
		audited = new ArrayList<>();
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
		assertEquals(DateHelper.todayInUtc().plusDays(LOAN_DAYS), registered.getDueDate());

		assertEquals(1, loans.created().size());
		final LoanEntity saved = loans.created().get(0);
		assertEquals(registered.getId(), saved.getId());
		assertEquals(BOOK_COPY, saved.getBookCopy().getId());
		assertEquals(USER, saved.getUser().getId());
		assertEquals(LoanEntity.STATUS_ACTIVE, saved.getStatus());
	}

	@Test
	@DisplayName("marca el ejemplar como prestado")
	void marksBookCopyAsLent() {
		useCase().execute(request());

		assertEquals(1, bookCopies.updated().size());
		assertEquals(BookCopyEntity.STATUS_LENT, bookCopies.updated().get(0).getStatus());
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
		assertTrue(bookCopies.updated().isEmpty());
		assertTrue(audited.isEmpty());
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
				() -> new LendBookUseCaseImpl(parameters, bookCopies, fines, loans, null, session, audit));

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.LOAN_UNEXPECTED, exception.getUserMessageKey());
	}

	private LendBookUseCaseImpl useCase() {
		return new LendBookUseCaseImpl(parameters, bookCopies, fines, loans, new LoanDomainMapperImpl(),
				new FakeSession(), this::audit);
	}

	private void audit(final String action, final UUID institution, final UUID person, final String detail) {
		audited.add(action + " " + institution + " " + person + " " + detail);
	}

	private static LendBookDomain request() {
		return new LendBookDomain(null, BOOK_COPY, USER, null, null, DateHelper.todayInUtc(), null);
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
