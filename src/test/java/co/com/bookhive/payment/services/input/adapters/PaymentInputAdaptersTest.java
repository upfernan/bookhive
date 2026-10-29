package co.com.bookhive.payment.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.ConfirmFinePaymentDomain;
import co.com.bookhive.payment.domain.finepayment.confirmfinepayment.FinePaymentStatusDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconcilePaymentsDomain;
import co.com.bookhive.payment.domain.finepayment.reconcilepayments.ReconciledPaymentsDomain;
import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.PaymentEventDomain;
import co.com.bookhive.payment.domain.finepayment.recordpaymentevent.RecordedPaymentEventDomain;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.FinePaymentCheckoutDomain;
import co.com.bookhive.payment.domain.finepayment.startfinepayment.StartFinePaymentDomain;
import co.com.bookhive.payment.services.input.dto.request.ConfirmFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.request.PaymentEventRequestDTO;
import co.com.bookhive.payment.services.input.dto.request.StartFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentCheckoutResponseDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentStatusResponseDTO;
import co.com.bookhive.payment.services.input.dto.response.ReconcilePaymentsResponseDTO;

class PaymentInputAdaptersTest {

	@Test
	@DisplayName("pedir el pago pasa la multa y devuelve todos los datos de la pasarela")
	void startsPayment() {
		final UUID fine = UUID.randomUUID();
		final List<StartFinePaymentDomain> received = new ArrayList<>();
		final StartFinePaymentInputAdapter adapter = new StartFinePaymentInputAdapter(domain -> {
			received.add(domain);
			return new FinePaymentCheckoutDomain("url", "pub", "COP", 300000L, "uco-1", "firma", "vence", "vuelta");
		});

		final FinePaymentCheckoutResponseDTO response = adapter.execute(new StartFinePaymentRequestDTO(fine));

		assertEquals(fine, received.get(0).fine());
		assertEquals(new FinePaymentCheckoutResponseDTO("url", "pub", "COP", 300000L, "uco-1", "firma", "vence", "vuelta"),
				response);
		assertEquals(UUIDHelper.getDefaultValue(), passThroughStart(adapter, received));
	}

	@Test
	@DisplayName("confirmar pasa referencia y transacción y devuelve el estado")
	void confirmsPayment() {
		final List<ConfirmFinePaymentDomain> received = new ArrayList<>();
		final ConfirmFinePaymentInputAdapter adapter = new ConfirmFinePaymentInputAdapter(domain -> {
			received.add(domain);
			return new FinePaymentStatusDomain(domain.reference(), "APPROVED", 300000L, true);
		});

		final FinePaymentStatusResponseDTO response = adapter.execute(new ConfirmFinePaymentRequestDTO("uco-1", "t-1"));
		adapter.execute(null);

		assertEquals(new ConfirmFinePaymentDomain("uco-1", "t-1"), received.get(0));
		assertEquals(new FinePaymentStatusResponseDTO("uco-1", "APPROVED", 300000L, true), response);
		assertEquals("", received.get(1).reference());
	}

	@Test
	@DisplayName("el aviso pasa todos sus datos al caso de uso")
	void recordsEvent() {
		final List<PaymentEventDomain> received = new ArrayList<>();
		final RecordPaymentEventInputAdapter adapter = new RecordPaymentEventInputAdapter(domain -> {
			received.add(domain);
			return new RecordedPaymentEventDomain(true);
		});

		assertTrue(adapter.execute(new PaymentEventRequestDTO(
				"uco-1", "t-1", "APPROVED", 300000L, List.of("t-1"), 10L, "firma")).applied());
		adapter.execute(null);

		assertEquals(new PaymentEventDomain("uco-1", "t-1", "APPROVED", 300000L, List.of("t-1"), 10L, "firma"),
				received.get(0));
		assertEquals("", received.get(1).checksum());
	}

	@Test
	@DisplayName("la revisión le deja al caso de uso decidir la hora y devuelve los conteos")
	void reconcilesPayments() {
		final List<ReconcilePaymentsDomain> received = new ArrayList<>();
		final ReconcilePaymentsInputAdapter adapter = new ReconcilePaymentsInputAdapter(domain -> {
			received.add(domain);
			return new ReconciledPaymentsDomain(1, 2, 1);
		});

		assertEquals(new ReconcilePaymentsResponseDTO(1, 2, 1), adapter.execute());
		assertEquals(DateHelper.getDefaultTimestamp(), received.get(0).now());
		assertTrue(received.get(0).now().isBefore(Instant.now()));
	}

	private static UUID passThroughStart(
			final StartFinePaymentInputAdapter adapter, final List<StartFinePaymentDomain> received) {
		adapter.execute(null);
		return received.get(received.size() - 1).fine();
	}

}
