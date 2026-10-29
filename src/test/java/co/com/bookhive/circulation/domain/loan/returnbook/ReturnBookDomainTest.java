package co.com.bookhive.circulation.domain.loan.returnbook;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class ReturnBookDomainTest {

	private static final UUID BOOK_COPY = UUID.randomUUID();
	private static final UUID INSTITUTION = UUID.randomUUID();
	private static final UUID LIBRARIAN = UUID.randomUUID();
	private static final LocalDate TODAY = DateHelper.todayIn(ZoneId.of(InstitutionParametersEntity.DEFAULT_TIME_ZONE));

	private final FakeRepositories.BookCopies bookCopies = new FakeRepositories.BookCopies()
			.with(FakeRepositories.bookCopy(BOOK_COPY, BookCopyEntity.STATUS_LENT, true));
	private final FakeRepositories.Loans loans = new FakeRepositories.Loans().withActive(new LoanEntity());
	private final FakeRepositories.Parameters parameters =
			new FakeRepositories.Parameters().with(15, 3, new BigDecimal("1000"));

	@Test
	@DisplayName("una devolución completa pasa la validación y busca el préstamo sin devolución del ejemplar")
	void validReturnPasses() {
		final ReturnBookDomain domain = returning(BOOK_COPY, TODAY);

		assertDoesNotThrow(domain::validate);
		assertTrue(domain.findOpenLoan().isPresent());
		assertEquals(BOOK_COPY, loans.searches().get(0).getBookCopy());
		assertTrue(loans.searches().get(0).onlyOpen());
	}

	@Test
	@DisplayName("la fecha de devolución es la de hoy en la zona horaria de la institución")
	void returnDateIsTodayInTheInstitutionZone() {
		final ReturnBookDomain domain = returning(BOOK_COPY, null);

		assertEquals(TODAY, domain.today());
		assertEquals(TODAY, domain.withReturnDate(domain.today()).getReturnDate());
		assertDoesNotThrow(domain.withReturnDate(domain.today())::validate);
	}

	@Test
	@DisplayName("cuenta los días de retraso desde la fecha límite y no cuenta días negativos")
	void countsOverdueDays() {
		final ReturnBookDomain domain = returning(BOOK_COPY, TODAY);

		assertEquals(3, domain.overdueDaysFor(TODAY.minusDays(3)));
		assertEquals(0, domain.overdueDaysFor(TODAY));
		assertEquals(0, domain.overdueDaysFor(TODAY.plusDays(4)));
		assertEquals(0, domain.overdueDaysFor(null));
	}

	@Test
	@DisplayName("la multa usa la tarifa diaria de la institución")
	void finePolicyUsesInstitutionRate() {
		assertEquals(new BigDecimal("2000.00"), returning(BOOK_COPY, TODAY).finePolicy().amountFor(2));
	}

	@Test
	@DisplayName("sin ejemplar, sesión o con fecha futura la devolución se rechaza con el mensaje correcto")
	void rejectsMissingData() {
		assertMessage(MessageKey.LOAN_RETURN_BOOK_COPY_REQUIRED, returning(null, TODAY));
		assertMessage(MessageKey.LOAN_LIBRARIAN_REQUIRED, new ReturnBookDomain(BOOK_COPY, INSTITUTION, null, TODAY)
				.withRepositories(bookCopies, loans, parameters));
		assertMessage(MessageKey.LOAN_INSTITUTION_REQUIRED, new ReturnBookDomain(BOOK_COPY, null, LIBRARIAN, TODAY)
				.withRepositories(bookCopies, loans, parameters));
		assertMessage(MessageKey.LOAN_DATE_REQUIRED, returning(BOOK_COPY, null));
		assertMessage(MessageKey.LOAN_DATE_IN_THE_FUTURE, returning(BOOK_COPY, TODAY.plusDays(1)));
	}

	@Test
	@DisplayName("sin parámetros, sin ejemplar o sin préstamo abierto la devolución no procede")
	void rejectsWhenStateDoesNotAllowIt() {
		final ReturnBookDomain withoutParameters = new ReturnBookDomain(BOOK_COPY, INSTITUTION, LIBRARIAN, TODAY)
				.withRepositories(bookCopies, loans, new FakeRepositories.Parameters());
		final ReturnBookDomain unknownCopy = returning(UUID.randomUUID(), TODAY);
		final ReturnBookDomain withoutLoan = new ReturnBookDomain(BOOK_COPY, INSTITUTION, LIBRARIAN, TODAY)
				.withRepositories(bookCopies, new FakeRepositories.Loans(), parameters);

		assertMessage(MessageKey.INSTITUTION_WITHOUT_PARAMETERS, withoutParameters);
		assertMessage(MessageKey.BOOK_COPY_NOT_FOUND, unknownCopy);
		assertTrue(withoutLoan.findOpenLoan().isEmpty());
		assertEquals(MessageKey.LOAN_RETURN_NOT_OPEN, withoutLoan.notOpen().getUserMessageKey());
		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, withoutLoan.notOpen().getKind());
	}

	@Test
	@DisplayName("la sesión completa institución y bibliotecario sin perder los repositorios")
	void sessionKeepsRepositories() {
		final ReturnBookDomain domain = new ReturnBookDomain(BOOK_COPY, null, null, TODAY)
				.withRepositories(bookCopies, loans, parameters)
				.withSession(INSTITUTION, LIBRARIAN);

		assertEquals(INSTITUTION, domain.getInstitution());
		assertEquals(LIBRARIAN, domain.getLibrarian());
		assertEquals(TODAY, domain.getReturnDate());
		assertDoesNotThrow(domain::validate);
	}

	@Test
	@DisplayName("usar el dominio sin repositorios es un error inesperado")
	void requiresRepositories() {
		final ReturnBookDomain domain = new ReturnBookDomain(BOOK_COPY, INSTITUTION, LIBRARIAN, TODAY);

		final BookHiveException exception = assertThrows(BookHiveException.class, domain::validate);

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertThrows(BookHiveException.class, domain::findOpenLoan);
		assertThrows(BookHiveException.class, domain::finePolicy);
		assertThrows(BookHiveException.class, domain::today);
	}

	private ReturnBookDomain returning(final UUID bookCopy, final LocalDate returnDate) {
		return new ReturnBookDomain(bookCopy, INSTITUTION, LIBRARIAN, returnDate)
				.withRepositories(bookCopies, loans, parameters);
	}

	private static void assertMessage(final String key, final ReturnBookDomain domain) {
		assertEquals(key, assertThrows(BookHiveException.class, domain::validate).getUserMessageKey());
	}

}
