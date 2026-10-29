package co.com.bookhive.catalog.infrastructure.output.externalservices.circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.services.output.ports.entities.UserReservationEntity;
import co.com.bookhive.circulation.services.input.dto.response.UserReservationResponseDTO;

class CirculationUserReservationsAdapterTest {

	@Test
	@DisplayName("pide a circulación las reservas de la sesión y las pasa al catálogo")
	void mapsCirculationReservations() {
		final UUID reservation = UUID.randomUUID();
		final UUID book = UUID.randomUUID();
		final Instant requestedAt = Instant.parse("2026-10-20T10:00:00Z");
		final LocalDate deadline = LocalDate.of(2026, 10, 26);
		final CirculationUserReservationsAdapter adapter = new CirculationUserReservationsAdapter(
				() -> List.of(new UserReservationResponseDTO(reservation, book, "EN_ESPERA", 4, requestedAt, deadline)));

		assertEquals(List.of(new UserReservationEntity(reservation, book, "EN_ESPERA", 4, requestedAt, deadline)),
				adapter.findMine());
	}

}
