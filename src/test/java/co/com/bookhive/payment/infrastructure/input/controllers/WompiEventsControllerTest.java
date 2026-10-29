package co.com.bookhive.payment.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import co.com.bookhive.payment.services.input.dto.request.PaymentEventRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.PaymentEventResponseDTO;

class WompiEventsControllerTest {

	private static final String EVENT = """
			{
			  "event": "transaction.updated",
			  "data": {
			    "transaction": {
			      "id": "12-1761480000-4920",
			      "amount_in_cents": 300000,
			      "reference": "uco-abc",
			      "status": "APPROVED"
			    }
			  },
			  "environment": "test",
			  "signature": {
			    "properties": ["transaction.id", "transaction.status", "transaction.amount_in_cents"],
			    "checksum": "firma-del-cuerpo"
			  },
			  "timestamp": 1761480000,
			  "sent_at": "2026-10-26T12:00:00.000Z"
			}
			""";

	private final List<PaymentEventRequestDTO> received = new ArrayList<>();

	@Test
	@DisplayName("arma el aviso con los valores firmados en el orden que indica Wompi")
	void buildsEventFromBody() {
		assertEquals(HttpStatus.OK, controller().receive(EVENT, null).getStatusCode());

		final PaymentEventRequestDTO event = received.get(0);
		assertEquals("uco-abc", event.reference());
		assertEquals("12-1761480000-4920", event.transaction());
		assertEquals("APPROVED", event.status());
		assertEquals(300000L, event.amountInCents());
		assertEquals(List.of("12-1761480000-4920", "APPROVED", "300000"), event.signedValues());
		assertEquals(1761480000L, event.timestamp());
		assertEquals("firma-del-cuerpo", event.checksum());
	}

	@Test
	@DisplayName("si llega la firma en el encabezado se usa esa")
	void prefersHeaderChecksum() {
		controller().receive(EVENT, "firma-del-encabezado");

		assertEquals("firma-del-encabezado", received.get(0).checksum());
	}

	@Test
	@DisplayName("los avisos de otro tipo se aceptan sin procesarlos")
	void ignoresOtherEvents() {
		assertEquals(HttpStatus.OK, controller().receive("{\"event\":\"nequi_token.updated\"}", null).getStatusCode());

		assertTrue(received.isEmpty());
	}

	@Test
	@DisplayName("un cuerpo vacío o que no es JSON se acepta sin procesarlo, para que la verificación de Wompi pase")
	void acceptsEmptyOrInvalidBodyWithoutProcessing() {
		assertEquals(HttpStatus.OK, controller().receive("no es json", null).getStatusCode());
		assertEquals(HttpStatus.OK, controller().receive(null, null).getStatusCode());
		assertEquals(HttpStatus.OK, controller().receive("", null).getStatusCode());
		assertTrue(received.isEmpty());
	}

	@Test
	@DisplayName("una consulta GET o HEAD a la dirección de avisos responde 200")
	void answersVerification() {
		assertEquals(HttpStatus.OK, controller().check().getStatusCode());
	}

	private WompiEventsController controller() {
		return new WompiEventsController(event -> {
			received.add(event);
			return new PaymentEventResponseDTO(true);
		});
	}

}
