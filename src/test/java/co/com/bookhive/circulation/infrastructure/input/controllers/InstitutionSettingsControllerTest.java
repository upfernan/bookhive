package co.com.bookhive.circulation.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.com.bookhive.circulation.domain.institution.configureinstitution.InstitutionSettingsDomain;
import co.com.bookhive.circulation.services.input.adapters.InstitutionSettingsInputAdapters;
import co.com.bookhive.circulation.services.input.dto.request.InstitutionSettingsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.InstitutionSettingsResponseDTO;

class InstitutionSettingsControllerTest {

	private static final InstitutionSettingsDomain CURRENT =
			new InstitutionSettingsDomain(15, 3, new BigDecimal("1000.00"), "America/Bogota");

	private final List<InstitutionSettingsDomain> received = new ArrayList<>();

	@Test
	@DisplayName("consulta las reglas vigentes por el puerto de entrada")
	void findsSettings() {
		assertEquals(new InstitutionSettingsResponseDTO(15, 3, new BigDecimal("1000.00"), "America/Bogota"),
				controller().find().getBody());
	}

	@Test
	@DisplayName("cambiar las reglas pasa todos los valores al caso de uso")
	void updatesSettings() {
		final var response = controller().update(
				new InstitutionSettingsRequestDTO(20, 4, new BigDecimal("2000"), "America/Bogota"));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new InstitutionSettingsDomain(20, 4, new BigDecimal("2000"), "America/Bogota"), received.get(0));
		assertEquals(20, response.getBody().loanDays());
	}

	@Test
	@DisplayName("sin petición pasa valores por defecto para que el caso de uso los rechace")
	void passesDefaultsWithoutRequest() {
		InstitutionSettingsInputAdapters.configure(this::receive).execute(null);

		assertEquals(new InstitutionSettingsDomain(0, 0, BigDecimal.ZERO, ""), received.get(0));
	}

	private InstitutionSettingsController controller() {
		return new InstitutionSettingsController(
				InstitutionSettingsInputAdapters.find(() -> CURRENT),
				InstitutionSettingsInputAdapters.configure(this::receive));
	}

	private InstitutionSettingsDomain receive(final InstitutionSettingsDomain settings) {
		received.add(settings);
		return settings;
	}

}
