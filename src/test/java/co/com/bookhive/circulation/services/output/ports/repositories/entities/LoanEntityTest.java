package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class LoanEntityTest {

	@Test
	@DisplayName("nace activo, con ejemplar y usuario vacíos pero nunca nulos")
	void startsWithDefaults() {
		final LoanEntity loan = new LoanEntity();

		assertEquals(UUIDHelper.getDefaultValue(), loan.getId());
		assertNotNull(loan.getBookCopy());
		assertNotNull(loan.getUser());
		assertEquals(UUIDHelper.getDefaultValue(), loan.getLibrarian());
		assertEquals(DateHelper.getDefaultValue(), loan.getLoanDate());
		assertEquals(DateHelper.getDefaultValue(), loan.getDueDate());
		assertEquals(LoanEntity.STATUS_ACTIVE, loan.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), loan.getCreatedAt());
	}

	@Test
	@DisplayName("guarda el ejemplar y el usuario como objetos")
	void keepsValues() {
		final UUID id = UUIDHelper.generateNew();
		final UUID librarian = UUIDHelper.generateNew();
		final BookCopyEntity bookCopy = new BookCopyEntity();
		final UserEntity user = new UserEntity();
		final LocalDate loanDate = LocalDate.of(2026, 10, 17);
		final Instant createdAt = Instant.parse("2026-10-17T15:00:00Z");
		final LoanEntity loan = new LoanEntity();

		loan.setId(id);
		loan.setBookCopy(bookCopy);
		loan.setUser(user);
		loan.setLibrarian(librarian);
		loan.setLoanDate(loanDate);
		loan.setDueDate(loanDate.plusDays(15));
		loan.setStatus(" " + LoanEntity.STATUS_RETURNED + " ");
		loan.setCreatedAt(createdAt);

		assertEquals(id, loan.getId());
		assertEquals(bookCopy, loan.getBookCopy());
		assertEquals(user, loan.getUser());
		assertEquals(librarian, loan.getLibrarian());
		assertEquals(loanDate, loan.getLoanDate());
		assertEquals(loanDate.plusDays(15), loan.getDueDate());
		assertEquals(LoanEntity.STATUS_RETURNED, loan.getStatus());
		assertEquals(createdAt, loan.getCreatedAt());
	}

	@Test
	@DisplayName("convierte los nulos y el estado vacío en valores por defecto")
	void replacesNulls() {
		final LoanEntity loan = new LoanEntity();
		loan.setStatus(LoanEntity.STATUS_OVERDUE);

		loan.setId(null);
		loan.setBookCopy(null);
		loan.setUser(null);
		loan.setLibrarian(null);
		loan.setLoanDate(null);
		loan.setDueDate(null);
		loan.setStatus(null);
		loan.setCreatedAt(null);

		assertEquals(UUIDHelper.getDefaultValue(), loan.getId());
		assertNotNull(loan.getBookCopy());
		assertNotNull(loan.getUser());
		assertEquals(UUIDHelper.getDefaultValue(), loan.getLibrarian());
		assertEquals(DateHelper.getDefaultValue(), loan.getLoanDate());
		assertEquals(DateHelper.getDefaultValue(), loan.getDueDate());
		assertEquals(LoanEntity.STATUS_ACTIVE, loan.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), loan.getCreatedAt());
	}

}
