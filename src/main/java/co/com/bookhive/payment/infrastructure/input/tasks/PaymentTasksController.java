package co.com.bookhive.payment.infrastructure.input.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.payment.services.input.dto.response.ReconcilePaymentsResponseDTO;
import co.com.bookhive.payment.services.input.ports.ReconcilePaymentsInputPort;

@RestController
public class PaymentTasksController {

	public static final String PATH = "/api/v1/tareas/conciliar-pagos";

	private static final Logger logger = LoggerFactory.getLogger(PaymentTasksController.class);

	private final ReconcilePaymentsInputPort reconcilePayments;

	public PaymentTasksController(final ReconcilePaymentsInputPort reconcilePayments) {
		super();
		this.reconcilePayments = reconcilePayments;
	}

	@PostMapping(PATH)
	public ResponseEntity<ReconcilePaymentsResponseDTO> reconcile() {
		logger.info("Cloud Scheduler pidió revisar en la pasarela los pagos de multas sin respuesta.");
		final ReconcilePaymentsResponseDTO response = reconcilePayments.execute();
		final int reviewed = response.reviewed();
		final int finesPaid = response.finesPaid();
		logger.info("Pagos revisados: {}; multas saldadas en la revisión: {}.", reviewed, finesPaid);
		return ResponseEntity.ok(response);
	}

}
