package co.com.bookhive.catalog.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.catalog.domain.reservation.listmyreservations.MyReservationDomain;
import co.com.bookhive.catalog.services.input.adapters.ListMyReservationsInputAdapter;
import co.com.bookhive.catalog.services.input.dto.response.MyReservationResponseDTO;

class MyReservationsControllerTest {

	@Test
	@DisplayName("responde 200 con las reservas del usuario, ya con título")
	void respondsWithReservations() {
		final UUID reservation = UUID.randomUUID();
		final UUID book = UUID.randomUUID();
		final Instant requestedAt = Instant.parse("2026-10-20T10:00:00Z");
		final MyReservationsController controller = new MyReservationsController(new ListMyReservationsInputAdapter(
				() -> List.of(new MyReservationDomain(reservation, book, "Clean Code", "Robert C. Martin",
						"EN_ESPERA", 1, requestedAt, null))));

		final ResponseEntity<List<MyReservationResponseDTO>> response = controller.listMine();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(List.of(new MyReservationResponseDTO(reservation, book, "Clean Code", "Robert C. Martin",
				"EN_ESPERA", 1, requestedAt, LocalDate.of(1900, 1, 1))), response.getBody());
	}

	@Test
	@DisplayName("los registros convierten los nulos en valores por defecto")
	void recordsReplaceNulls() {
		final MyReservationResponseDTO empty = new MyReservationResponseDTO(null, null, null, null, null, -1, null, null);
		final MyReservationDomain domain = new MyReservationDomain(null, null, null, null, null, -1, null, null);

		assertEquals("", empty.title());
		assertEquals(0, empty.position());
		assertEquals("", domain.authors());
		assertEquals(0, domain.position());
	}

}
