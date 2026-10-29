package co.com.bookhive.payment.infrastructure.input.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;
import co.com.bookhive.payment.services.input.dto.request.ConfirmFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.request.StartFinePaymentRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentCheckoutResponseDTO;
import co.com.bookhive.payment.services.input.dto.response.FinePaymentStatusResponseDTO;
import co.com.bookhive.payment.services.input.ports.ConfirmFinePaymentInputPort;
import co.com.bookhive.payment.services.input.ports.StartFinePaymentInputPort;

@RestController
@RequestMapping("/api/v1/pagos/multas")
public class FinePaymentController {

	private static final Logger logger = LoggerFactory.getLogger(FinePaymentController.class);

	private final StartFinePaymentInputPort startFinePayment;
	private final ConfirmFinePaymentInputPort confirmFinePayment;

	public FinePaymentController(
			final StartFinePaymentInputPort startFinePayment,
			final ConfirmFinePaymentInputPort confirmFinePayment) {

		super();
		this.startFinePayment = startFinePayment;
		this.confirmFinePayment = confirmFinePayment;
	}

	@RequiredPermission(PermissionCode.PAY_FINE)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping
	public ResponseEntity<FinePaymentCheckoutResponseDTO> start(@RequestBody final StartFinePaymentRequestDTO request) {
		logger.info("Inicia el pago en línea de una multa.");
		final FinePaymentCheckoutResponseDTO response = startFinePayment.execute(request);
		final String reference = response.reference();
		logger.info("Pago {} listo para enviarse a la pasarela.", reference);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@RequiredPermission(PermissionCode.PAY_FINE)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping("/{reference}/confirmacion")
	public ResponseEntity<FinePaymentStatusResponseDTO> confirm(
			@PathVariable final String reference,
			@RequestBody final ConfirmFinePaymentRequestDTO request) {

		logger.info("Se consulta en la pasarela el resultado de un pago.");
		final FinePaymentStatusResponseDTO response = confirmFinePayment.execute(
				new ConfirmFinePaymentRequestDTO(reference, request.transaction()));
		final String status = response.status();
		logger.info("El pago {} quedó en estado {}.", response.reference(), status);
		return ResponseEntity.ok(response);
	}

}
