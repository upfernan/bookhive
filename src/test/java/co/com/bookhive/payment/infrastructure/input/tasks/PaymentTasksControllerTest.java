package co.com.bookhive.payment.infrastructure.input.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.payment.services.input.dto.response.ReconcilePaymentsResponseDTO;

class PaymentTasksControllerTest {

	@Test
	@DisplayName("la tarea programada revisa los pagos y responde cuántos revisó")
	void reconcilesPayments() {
		final ResponseEntity<ReconcilePaymentsResponseDTO> response =
				new PaymentTasksController(() -> new ReconcilePaymentsResponseDTO(2, 3, 1)).reconcile();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new ReconcilePaymentsResponseDTO(2, 3, 1), response.getBody());
	}

}
