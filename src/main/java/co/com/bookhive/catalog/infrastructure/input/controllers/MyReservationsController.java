package co.com.bookhive.catalog.infrastructure.input.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.catalog.services.input.dto.response.MyReservationResponseDTO;
import co.com.bookhive.catalog.services.input.ports.ListMyReservationsInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
@RequestMapping("/api/v1/reservas")
public class MyReservationsController {

	private static final Logger logger = LoggerFactory.getLogger(MyReservationsController.class);

	private final ListMyReservationsInputPort listMyReservationsInputPort;

	public MyReservationsController(final ListMyReservationsInputPort listMyReservationsInputPort) {
		super();
		this.listMyReservationsInputPort = listMyReservationsInputPort;
	}

	@RequiredPermission(PermissionCode.RESERVE_BOOK)
	@Transactional(readOnly = true)
	@GetMapping
	public ResponseEntity<List<MyReservationResponseDTO>> listMine() {
		logger.info("Inicia la consulta de las reservas del usuario.");
		final List<MyReservationResponseDTO> mine = listMyReservationsInputPort.execute();
		logger.info("El usuario tiene {} reserva(s) activa(s).", mine.size());
		return ResponseEntity.ok(mine);
	}

}
