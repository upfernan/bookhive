package co.com.bookhive.circulation.infrastructure.input.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.circulation.services.input.dto.request.CancelReservationRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.ReserveBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.CancelReservationResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReserveBookResponseDTO;
import co.com.bookhive.circulation.services.input.ports.CancelReservationInputPort;
import co.com.bookhive.circulation.services.input.ports.ReserveBookInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservationController {

	private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

	private final ReserveBookInputPort reserveBookInputPort;
	private final CancelReservationInputPort cancelReservationInputPort;

	public ReservationController(
			final ReserveBookInputPort reserveBookInputPort,
			final CancelReservationInputPort cancelReservationInputPort) {

		super();
		this.reserveBookInputPort = reserveBookInputPort;
		this.cancelReservationInputPort = cancelReservationInputPort;
	}

	@RequiredPermission(PermissionCode.RESERVE_BOOK)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping
	public ResponseEntity<ReserveBookResponseDTO> reserve(@RequestBody final ReserveBookRequestDTO request) {
		logger.info("Inicia el registro de una reserva.");
		final ReserveBookResponseDTO response = reserveBookInputPort.execute(request);
		final int position = response.position();
		logger.info("Reserva registrada en el puesto {} de la fila.", position);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@RequiredPermission(PermissionCode.RESERVE_BOOK)
	@Transactional(rollbackFor = Exception.class)
	@DeleteMapping("/{reservation}")
	public ResponseEntity<CancelReservationResponseDTO> cancel(@PathVariable final UUID reservation) {
		logger.info("Inicia la cancelación de una reserva.");
		final CancelReservationResponseDTO response =
				cancelReservationInputPort.execute(new CancelReservationRequestDTO(reservation));
		logger.info("Reserva cancelada.");
		return ResponseEntity.ok(response);
	}

}
