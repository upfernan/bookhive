package co.com.bookhive.circulation.services.usecase.fine.findpayablefine.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.fine.findpayablefine.FindPayableFineDomain;
import co.com.bookhive.circulation.domain.fine.findpayablefine.PayableFineDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindPayableFineUseCaseImplTest {

	private static final UUID FINE = UUID.randomUUID();
	private static final UUID USER = UUID.randomUUID();
	private static final BigDecimal AMOUNT = new BigDecimal("3000.00");

	@Test
	@DisplayName("una multa propia sin pago registrado se puede pagar por su valor")
	void pendingFineIsPayable() {
		final FakeRepositories.Fines fines = new FakeRepositories.Fines().withFine(fine(USER));

		final PayableFineDomain payable = new FindPayableFineUseCaseImpl(fines).execute(new FindPayableFineDomain(FINE, USER));

		assertEquals(FINE, payable.fine());
		assertEquals(AMOUNT, payable.amount());
		assertTrue(payable.pending());
	}

	@Test
	@DisplayName("una multa ya saldada se informa como no pendiente")
	void settledFineIsNotPending() {
		final FakeRepositories.Fines fines = new FakeRepositories.Fines().withSettledFine(fine(USER));

		final PayableFineDomain payable = new FindPayableFineUseCaseImpl(fines).execute(new FindPayableFineDomain(FINE, USER));

		assertFalse(payable.pending());
	}

	@Test
	@DisplayName("la multa de otra persona no se encuentra")
	void fineOfAnotherUserIsNotFound() {
		final FindPayableFineUseCaseImpl useCase =
				new FindPayableFineUseCaseImpl(new FakeRepositories.Fines().withFine(fine(UUID.randomUUID())));
		final FindPayableFineDomain request = new FindPayableFineDomain(FINE, USER);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(MessageKey.FINE_NOT_FOUND, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("sin multa se pide indicar cuál")
	void requiresFine() {
		final FindPayableFineUseCaseImpl useCase = new FindPayableFineUseCaseImpl(new FakeRepositories.Fines());

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(MessageKey.FINE_ID_REQUIRED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("sin quien paga es un error inesperado")
	void requiresPayer() {
		final FindPayableFineUseCaseImpl useCase = new FindPayableFineUseCaseImpl(new FakeRepositories.Fines());
		final FindPayableFineDomain request = new FindPayableFineDomain(FINE, null);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(request));

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

	@Test
	@DisplayName("no se crea sin el repositorio de multas")
	void requiresRepository() {
		final BookHiveException exception =
				assertThrows(BookHiveException.class, () -> new FindPayableFineUseCaseImpl(null));

		assertEquals(MessageKey.FINE_UNEXPECTED, exception.getUserMessageKey());
	}

	private static FineEntity fine(final UUID owner) {
		final FineEntity fine = new FineEntity();
		fine.setId(FINE);
		fine.setUser(FakeRepositories.user(owner, "Ana", "ana@uco.edu.co"));
		fine.setTotalAmount(AMOUNT);
		return fine;
	}

}
