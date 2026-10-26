package co.com.bookhive.payment.infrastructure.output.externalservices.circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.input.dto.request.FindPayableFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.SettleFineRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.PayableFineResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.SettleFineResponseDTO;
import co.com.bookhive.payment.services.output.ports.entities.PayableFineEntity;
import co.com.bookhive.payment.services.output.ports.entities.SettledFineEntity;

class CirculationFinesAdapterTest {

	private static final UUID FINE = UUID.randomUUID();
	private static final UUID PAYER = UUID.randomUUID();

	@Test
	@DisplayName("consulta y salda la multa por los puertos de entrada de circulación")
	void usesCirculationInputPorts() {
		final List<Object> received = new ArrayList<>();
		final CirculationFinesAdapter adapter = new CirculationFinesAdapter(
				request -> {
					received.add(request);
					return new PayableFineResponseDTO(request.fine(), new BigDecimal("3000"), true);
				},
				request -> {
					received.add(request);
					return new SettleFineResponseDTO(true, request.paymentReference());
				});

		final PayableFineEntity payable = adapter.find(FINE, PAYER);
		final SettledFineEntity settled = adapter.settle(FINE, "uco-1", new BigDecimal("3000"));

		assertEquals(new PayableFineEntity(FINE, new BigDecimal("3000"), true), payable);
		assertTrue(settled.newlySettled());
		assertEquals("uco-1", settled.paymentReference());
		assertEquals(new FindPayableFineRequestDTO(FINE, PAYER), received.get(0));
		assertEquals(new SettleFineRequestDTO(FINE, "uco-1", new BigDecimal("3000")), received.get(1));
	}

}
