package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities;

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

class FineJpaEntityTest {

	@Test
	@DisplayName("nace pendiente, en cero y con préstamo y usuario nunca nulos")
	void startsWithDefaults() {
		final FineJpaEntity fine = new FineJpaEntity();

		assertEquals(UUIDHelper.getDefaultValue(), fine.getId());
		assertNotNull(fine.getLoan());
		assertNotNull(fine.getUser());
		assertEquals(DateHelper.getDefaultValue(), fine.getIssueDate());
		assertEquals(0, fine.getOverdueDays());
		assertEquals(BigDecimal.ZERO, fine.getDailyRate());
		assertEquals(BigDecimal.ZERO, fine.getTotalAmount());
		assertEquals(FineJpaEntity.STATUS_PENDING, fine.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), fine.getCreatedAt());
	}

	@Test
	@DisplayName("guarda el préstamo, el usuario y los valores de la multa")
	void keepsValues() {
		final UUID id = UUIDHelper.generateNew();
		final LoanJpaEntity loan = new LoanJpaEntity();
		final UserJpaEntity user = new UserJpaEntity();
		final LocalDate issueDate = LocalDate.of(2026, 10, 17);
		final Instant createdAt = Instant.parse("2026-10-17T15:00:00Z");
		final FineJpaEntity fine = new FineJpaEntity();

		fine.setId(id);
		fine.setLoan(loan);
		fine.setUser(user);
		fine.setIssueDate(issueDate);
		fine.setOverdueDays(3);
		fine.setDailyRate(new BigDecimal("1500.50"));
		fine.setTotalAmount(new BigDecimal("4501.50"));
		fine.setStatus(" " + FineJpaEntity.STATUS_PAID + " ");
		fine.setCreatedAt(createdAt);

		assertEquals(id, fine.getId());
		assertEquals(loan, fine.getLoan());
		assertEquals(user, fine.getUser());
		assertEquals(issueDate, fine.getIssueDate());
		assertEquals(3, fine.getOverdueDays());
		assertEquals(new BigDecimal("1500.50"), fine.getDailyRate());
		assertEquals(new BigDecimal("4501.50"), fine.getTotalAmount());
		assertEquals(FineJpaEntity.STATUS_PAID, fine.getStatus());
		assertEquals(createdAt, fine.getCreatedAt());
	}

	@Test
	@DisplayName("convierte los nulos y los días negativos en valores por defecto")
	void replacesNulls() {
		final FineJpaEntity fine = new FineJpaEntity();
		fine.setStatus(FineJpaEntity.STATUS_PAID);

		fine.setId(null);
		fine.setLoan(null);
		fine.setUser(null);
		fine.setIssueDate(null);
		fine.setOverdueDays(-4);
		fine.setDailyRate(null);
		fine.setTotalAmount(null);
		fine.setStatus(null);
		fine.setCreatedAt(null);

		assertEquals(UUIDHelper.getDefaultValue(), fine.getId());
		assertNotNull(fine.getLoan());
		assertNotNull(fine.getUser());
		assertEquals(DateHelper.getDefaultValue(), fine.getIssueDate());
		assertEquals(0, fine.getOverdueDays());
		assertEquals(BigDecimal.ZERO, fine.getDailyRate());
		assertEquals(BigDecimal.ZERO, fine.getTotalAmount());
		assertEquals(FineJpaEntity.STATUS_PENDING, fine.getStatus());
		assertEquals(DateHelper.getDefaultTimestamp(), fine.getCreatedAt());
	}

}
