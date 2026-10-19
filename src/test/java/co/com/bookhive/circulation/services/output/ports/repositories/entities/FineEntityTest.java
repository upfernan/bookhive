package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

class FineEntityTest {

	@Test
	@DisplayName("nace pendiente, en cero y con préstamo y usuario nunca nulos")
	void startsWithDefaults() {
		final FineEntity fine = new FineEntity();

		assertEquals(UUIDHelper.getDefaultValue(), fine.getId());
		assertNotNull(fine.getLoan());
		assertNotNull(fine.getUser());
		assertEquals(DateHelper.getDefaultValue(), fine.getIssueDate());
		assertEquals(0, fine.getOverdueDays());
		assertEquals(BigDecimal.ZERO, fine.getDailyRate());
		assertEquals(BigDecimal.ZERO, fine.getTotalAmount());
		assertEquals(FineEntity.STATUS_PENDING, fine.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), fine.getCreatedAt());
	}

	@Test
	@DisplayName("guarda el préstamo, el usuario y los valores de la multa")
	void keepsValues() {
		final UUID id = UUIDHelper.generateNew();
		final LoanEntity loan = new LoanEntity();
		final UserEntity user = new UserEntity();
		final LocalDate issueDate = LocalDate.of(2026, 10, 17);
		final Instant createdAt = Instant.parse("2026-10-17T15:00:00Z");
		final FineEntity fine = new FineEntity();

		fine.setId(id);
		fine.setLoan(loan);
		fine.setUser(user);
		fine.setIssueDate(issueDate);
		fine.setOverdueDays(3);
		fine.setDailyRate(new BigDecimal("1500.50"));
		fine.setTotalAmount(new BigDecimal("4501.50"));
		fine.setStatus(" " + FineEntity.STATUS_PAID + " ");
		fine.setCreatedAt(createdAt);

		assertEquals(id, fine.getId());
		assertEquals(loan, fine.getLoan());
		assertEquals(user, fine.getUser());
		assertEquals(issueDate, fine.getIssueDate());
		assertEquals(3, fine.getOverdueDays());
		assertEquals(new BigDecimal("1500.50"), fine.getDailyRate());
		assertEquals(new BigDecimal("4501.50"), fine.getTotalAmount());
		assertEquals(FineEntity.STATUS_PAID, fine.getStatus());
		assertEquals(createdAt, fine.getCreatedAt());
	}

	@Test
	@DisplayName("convierte los nulos, los días negativos y el estado vacío en valores por defecto")
	void replacesNulls() {
		final FineEntity fine = new FineEntity();
		fine.setStatus(FineEntity.STATUS_WAIVED);

		fine.setId(null);
		fine.setLoan(null);
		fine.setUser(null);
		fine.setIssueDate(null);
		fine.setOverdueDays(-4);
		fine.setDailyRate(null);
		fine.setTotalAmount(null);
		fine.setStatus("");
		fine.setCreatedAt(null);

		assertEquals(UUIDHelper.getDefaultValue(), fine.getId());
		assertNotNull(fine.getLoan());
		assertNotNull(fine.getUser());
		assertEquals(DateHelper.getDefaultValue(), fine.getIssueDate());
		assertEquals(0, fine.getOverdueDays());
		assertEquals(BigDecimal.ZERO, fine.getDailyRate());
		assertEquals(BigDecimal.ZERO, fine.getTotalAmount());
		assertEquals(FineEntity.STATUS_PENDING, fine.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), fine.getCreatedAt());
	}

}
