package co.com.bookhive.payment.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.payment.services.input.dto.request.ConfirmFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.request.StartFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentCheckoutResponseDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentStatusResponseDTO;

class FinePaymentControllerTest {

	private final List<ConfirmFinePaymentRequestDTO> confirmations = new ArrayList<>();

	@Test
	@DisplayName("pedir el pago responde 201 con los datos para la pasarela")
	void startsPayment() {
		final ResponseEntity<FinePaymentCheckoutResponseDTO> response =
				controller().start(new StartFinePaymentRequestDTO(UUID.randomUUID()));

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("uco-1", response.getBody().reference());
	}

	@Test
	@DisplayName("confirmar usa la referencia de la dirección y la transacción del cuerpo")
	void confirmsPayment() {
		final ResponseEntity<FinePaymentStatusResponseDTO> response =
				controller().confirm("uco-1", new ConfirmFinePaymentRequestDTO("otra", "t-1"));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new ConfirmFinePaymentRequestDTO("uco-1", "t-1"), confirmations.get(0));
		assertEquals("APPROVED", response.getBody().status());
	}

	private FinePaymentController controller() {
		return new FinePaymentController(
				request -> new FinePaymentCheckoutResponseDTO("u", "k", "COP", 1L, "uco-1", "s", "e", "r"),
				request -> {
					confirmations.add(request);
					return new FinePaymentStatusResponseDTO(request.reference(), "APPROVED", 1L, true);
				});
	}

}
