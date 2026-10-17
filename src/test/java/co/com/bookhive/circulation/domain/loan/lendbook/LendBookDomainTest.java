package co.com.bookhive.circulation.domain.loan.lendbook;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class LendBookDomainTest {

	private static final UUID BOOK_COPY = UUIDHelper.generateNew();
	private static final UUID USER = UUIDHelper.generateNew();
	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID LIBRARIAN = UUIDHelper.generateNew();
	private static final int LOAN_DAYS = 15;
	private static final int MAX_LOANS = 3;

	private FakeRepositories.BookCopies bookCopies;
	private FakeRepositories.Fines fines;
	private FakeRepositories.Loans loans;
	private FakeRepositories.Parameters parameters;

	@BeforeEach
	void prepare() {
		bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_AVAILABLE, true));
		fines = new FakeRepositories.Fines();
		loans = new FakeRepositories.Loans();
		parameters = new FakeRepositories.Parameters().with(LOAN_DAYS, MAX_LOANS);
	}

	@Test
	@DisplayName("acepta un préstamo válido y calcula la fecha de entrega con los días de la institución")
	void acceptsValidLoan() {
		final LendBookDomain loan = loan(BOOK_COPY, USER, today());

		assertDoesNotThrow(loan::validate);
		assertEquals(today().plusDays(LOAN_DAYS), loan.calculateDueDate());
	}

	@Test
	@DisplayName("rechaza el préstamo sin ejemplar")
	void rejectsMissingBookCopy() {
		assertRejected(MessageKey.LOAN_BOOK_COPY_REQUIRED, loan(null, USER, today())::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo sin usuario")
	void rejectsMissingUser() {
		assertRejected(MessageKey.LOAN_USER_REQUIRED, loan(BOOK_COPY, null, today())::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo sin bibliotecario")
	void rejectsMissingLibrarian() {
		final LendBookDomain loan = new LendBookDomain(null, BOOK_COPY, USER, INSTITUTION, null, today(), null)
				.withRepositories(bookCopies, fines, loans, parameters);

		assertRejected(MessageKey.LOAN_LIBRARIAN_REQUIRED, loan::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo sin institución")
	void rejectsMissingInstitution() {
		final LendBookDomain loan = new LendBookDomain(null, BOOK_COPY, USER, null, LIBRARIAN, today(), null)
				.withRepositories(bookCopies, fines, loans, parameters);

		assertRejected(MessageKey.LOAN_INSTITUTION_REQUIRED, loan::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo sin fecha")
	void rejectsMissingDate() {
		assertRejected(MessageKey.LOAN_DATE_REQUIRED, loan(BOOK_COPY, USER, null)::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo con fecha futura")
	void rejectsFutureDate() {
		assertRejected(MessageKey.LOAN_DATE_IN_THE_FUTURE, loan(BOOK_COPY, USER, today().plusDays(1))::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo si la institución no tiene parámetros")
	void rejectsInstitutionWithoutParameters() {
		parameters = new FakeRepositories.Parameters();

		assertRejected(MessageKey.INSTITUTION_WITHOUT_PARAMETERS, loan(BOOK_COPY, USER, today())::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo si el ejemplar no existe")
	void rejectsUnknownBookCopy() {
		assertRejected(MessageKey.BOOK_COPY_NOT_FOUND, loan(UUIDHelper.generateNew(), USER, today())::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo si el ejemplar ya está prestado")
	void rejectsLentBookCopy() {
		bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_LENT, true));

		assertRejected(MessageKey.BOOK_COPY_NOT_AVAILABLE, loan(BOOK_COPY, USER, today())::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo si el ejemplar es solo de consulta en sala")
	void rejectsReferenceOnlyBookCopy() {
		bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_AVAILABLE, false));

		assertRejected(MessageKey.BOOK_COPY_REFERENCE_ONLY, loan(BOOK_COPY, USER, today())::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo si el usuario tiene multas pendientes")
	void rejectsUserWithPendingFines() {
		fines = new FakeRepositories.Fines().withPendingFine();

		assertRejected(MessageKey.USER_WITH_PENDING_FINES, loan(BOOK_COPY, USER, today())::validate);
	}

	@Test
	@DisplayName("rechaza el préstamo si el usuario ya llegó al máximo de préstamos")
	void rejectsUserAtLoanLimit() {
		loans = new FakeRepositories.Loans().withActiveLoans(MAX_LOANS);

		assertRejected(MessageKey.USER_AT_LOAN_LIMIT, loan(BOOK_COPY, USER, today())::validate);
	}

	@Test
	@DisplayName("no deja validar si no se le pasaron los repositorios")
	void rejectsDomainWithoutRepositories() {
		final LendBookDomain loan = new LendBookDomain(null, BOOK_COPY, USER, INSTITUTION, LIBRARIAN, today(), null);

		final BookHiveException exception = assertThrows(BookHiveException.class, loan::validate);

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

	@Test
	@DisplayName("convierte los datos nulos en valores por defecto")
	void replacesNullsWithDefaults() {
		final LendBookDomain loan = new LendBookDomain(null, null, null, null, null, null, null);

		assertEquals(UUIDHelper.getDefaultValue(), loan.getId());
		assertEquals(UUIDHelper.getDefaultValue(), loan.getBookCopy());
		assertEquals(DateHelper.getDefaultValue(), loan.getLoanDate());
	}

	private LendBookDomain loan(final UUID bookCopy, final UUID user, final LocalDate loanDate) {
		return new LendBookDomain(null, bookCopy, user, INSTITUTION, LIBRARIAN, loanDate, null)
				.withRepositories(bookCopies, fines, loans, parameters);
	}

	private static LocalDate today() {
		return DateHelper.todayInUtc();
	}

	private static void assertRejected(final String messageKey, final Executable action) {
		final BookHiveException exception = assertThrows(BookHiveException.class, action);
		assertEquals(messageKey, exception.getUserMessageKey());
	}

}
