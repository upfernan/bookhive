package co.com.bookhive.catalog.infrastructure.output.externalservices.circulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.input.dto.response.LocationResponseDTOs;

class CirculationBranchDirectoryAdapterTest {

	@Test
	@DisplayName("le dice al usuario el nombre de la sede donde está el ejemplar")
	void namesBranch() {
		final UUID centro = UUID.randomUUID();
		final CirculationBranchDirectoryAdapter adapter = new CirculationBranchDirectoryAdapter(
				() -> List.of(new LocationResponseDTOs.Branch(centro, "Centro")));

		assertEquals("Centro", adapter.nameOf(centro));
		assertEquals("", adapter.nameOf(UUID.randomUUID()));
	}

}
