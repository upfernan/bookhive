package co.com.bookhive.circulation.services.usecase.fine.settlefine.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.domain.fine.settlefine.SettleFineDomain;
import co.com.bookhive.circulation.domain.fine.settlefine.SettledFineDomain;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineSettlementEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class SettleFineUseCaseImplTest {

	private static final UUID FINE = UUID.randomUUID();
	private static final UUID USER = UUID.randomUUID();
	private static final UUID LOAN = UUID.randomUUID();
	private static final UUID BOOK = UUID.randomUUID();
	private static final BigDecimal AMOUNT = new BigDecimal("3000.00");
	private static final String REFERENCE = "uco-0190a0b1-0000-7000-8000-000000000001";

	private FakeRepositories.Fines fines;
	private FakeRepositories.FineSettlements settlements;
	private FakeRepositories.BookCopies bookCopies;
	private List<NotificationRequestEntity> notices;

	@BeforeEach
	void prepare() {
		final BookCopyEntity copy = FakeRepositories.bookCopyOf(BOOK, BookCopyEntity.STATUS_AVAILABLE);
		final LoanEntity loan = new LoanEntity();
		loan.setId(LOAN);
		loan.setBookCopy(copy);
		final FineEntity fine = new FineEntity();
		fine.setId(FINE);
		fine.setLoan(loan);
		fine.setUser(FakeRepositories.user(USER, "Ana", "ana@uco.edu.co"));
		fine.setTotalAmount(AMOUNT);
		fines = new FakeRepositories.Fines().withFine(fine);
		settlements = new FakeRepositories.FineSettlements();
		bookCopies = new FakeRepositories.BookCopies().with(copy);
		notices = new ArrayList<>();
	}

	@Test
	@DisplayName("el pago aprobado salda la multa una vez y avisa al usuario")
	void settlesFineAndNotifies() {
		final SettledFineDomain settled = useCase().execute(new SettleFineDomain(FINE, REFERENCE, AMOUNT));

		assertTrue(settled.newlySettled());
		assertEquals(REFERENCE, settled.paymentReference());
		assertEquals(1, settlements.settled().size());
		assertEquals(AMOUNT, settlements.settled().get(0).getAmount());
		assertEquals(1, notices.size());
		final NotificationRequestEntity notice = notices.get(0);
		assertEquals(SettleFineDomain.NOTICE_KIND, notice.kind());
		assertEquals(USER, notice.user());
		assertEquals(BOOK, notice.book());
		assertEquals(LOAN, notice.loan());
		assertEquals(DateHelper.todayIn(ZoneId.of(InstitutionParametersEntity.DEFAULT_TIME_ZONE)), notice.dueDate());
	}

	@Test
	@DisplayName("una multa ya saldada no se salda otra vez y dice con qué pago quedó")
	void repeatedSettlementKeepsTheFirst() {
		final FineSettlementEntity first = new FineSettlementEntity();
		first.setFine(FINE);
		first.setPaymentReference("uco-primero");
		settlements.with(first);

		final SettledFineDomain settled = useCase().execute(new SettleFineDomain(FINE, REFERENCE, AMOUNT));

		assertFalse(settled.newlySettled());
		assertEquals("uco-primero", settled.paymentReference());
		assertTrue(notices.isEmpty());
	}

	@Test
	@DisplayName("un pago que no cubre exactamente la multa no la salda")
	void rejectsDifferentAmount() {
		final SettleFineUseCaseImpl useCase = useCase();
		final SettleFineDomain request = new SettleFineDomain(FINE, REFERENCE, new BigDecimal("2000"));

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, exception.getKind());
		assertTrue(settlements.settled().isEmpty());
	}

	@Test
	@DisplayName("una multa que no existe es un error inesperado")
	void rejectsUnknownFine() {
		final SettleFineUseCaseImpl useCase = useCase();
		final SettleFineDomain request = new SettleFineDomain(UUID.randomUUID(), REFERENCE, AMOUNT);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

	@Test
	@DisplayName("sin referencia o sin valor no se salda nada")
	void requiresPaymentData() {
		final SettleFineUseCaseImpl useCase = useCase();
		final SettleFineDomain withoutReference = new SettleFineDomain(FINE, " ", AMOUNT);
		final SettleFineDomain withoutAmount = new SettleFineDomain(FINE, REFERENCE, BigDecimal.ZERO);

		assertEquals(MessageKey.FINE_PAYMENT_DATA_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(withoutReference)).getUserMessageKey());
		assertEquals(MessageKey.FINE_PAYMENT_DATA_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(withoutAmount)).getUserMessageKey());
		assertEquals(MessageKey.FINE_PAYMENT_DATA_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(null)).getUserMessageKey());
	}

	@Test
	@DisplayName("no se crea sin sus dependencias")
	void requiresDependencies() {
		final FakeRepositories.Parameters parameters = new FakeRepositories.Parameters();

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> new SettleFineUseCaseImpl(fines, settlements, bookCopies, parameters, null));

		assertEquals(MessageKey.FINE_UNEXPECTED, exception.getUserMessageKey());
	}

	private SettleFineUseCaseImpl useCase() {
		return new SettleFineUseCaseImpl(
				fines, settlements, bookCopies, new FakeRepositories.Parameters().with(15, 3), notices::add);
	}

}
