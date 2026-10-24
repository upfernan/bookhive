package co.com.bookhive.circulation.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.circulation.services.input.dto.request.CancelReservationRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.ReserveBookRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.CancelReservationResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.ReserveBookResponseDTO;

class ReservationControllerTest {

	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID RESERVATION = UUID.randomUUID();

	@Test
	@DisplayName("reservar responde 201 con el puesto en la fila")
	void reserveRespondsCreated() {
		final ReserveBookResponseDTO answer = new ReserveBookResponseDTO(RESERVATION, BOOK, "EN_ESPERA", 1);
		final ReservationController controller = new ReservationController(request -> answer, request -> null);

		final ResponseEntity<ReserveBookResponseDTO> response = controller.reserve(new ReserveBookRequestDTO(BOOK));

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(answer, response.getBody());
	}

	@Test
	@DisplayName("cancelar toma la reserva de la ruta y responde 200")
	void cancelRespondsOk() {
		final List<CancelReservationRequestDTO> received = new ArrayList<>();
		final CancelReservationResponseDTO answer = new CancelReservationResponseDTO(RESERVATION, BOOK, "CANCELADA");
		final ReservationController controller = new ReservationController(request -> null, request -> {
			received.add(request);
			return answer;
		});

		final ResponseEntity<CancelReservationResponseDTO> response = controller.cancel(RESERVATION);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(answer, response.getBody());
		assertEquals(RESERVATION, received.get(0).reservation());
	}

}
