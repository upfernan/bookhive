package co.com.bookhive.access.infrastructure.input.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.com.bookhive.access.domain.datapolicy.acceptdatapolicy.AcceptDataPolicyDomain;
import co.com.bookhive.access.domain.datapolicy.finddatapolicy.DataPolicyDomain;
import co.com.bookhive.access.services.input.adapters.AcceptDataPolicyInputAdapter;
import co.com.bookhive.access.services.input.adapters.FindDataPolicyInputAdapter;
import co.com.bookhive.access.services.input.dto.request.AcceptDataPolicyRequestDTO;
import co.com.bookhive.access.services.input.dto.response.DataPolicyResponseDTO;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class DataPolicyControllerTest {

	private static final Instant PUBLISHED = Instant.parse("2026-10-01T05:00:00Z");
	private static final Instant ACCEPTED = Instant.parse("2026-10-26T21:00:00Z");

	private final List<AcceptDataPolicyDomain> received = new ArrayList<>();

	@Test
	@DisplayName("muestra la política sin fecha de aceptación mientras no se haya aceptado")
	void findsPolicy() {
		final DataPolicyResponseDTO response = controller().find().getBody();

		assertEquals(new DataPolicyResponseDTO(2, "Texto", PUBLISHED, false, null), response);
	}

	@Test
	@DisplayName("aceptar pasa la versión leída y responde con la fecha de la constancia")
	void acceptsPolicy() {
		final var response = controller().accept(new AcceptDataPolicyRequestDTO(2));

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new AcceptDataPolicyDomain(2), received.get(0));
		assertTrue(response.getBody().accepted());
		assertEquals(ACCEPTED, response.getBody().acceptedAt());
	}

	@Test
	@DisplayName("sin cuerpo pasa la versión 0 para que se rechace")
	void acceptsWithoutBody() {
		controller().accept(null);

		assertEquals(new AcceptDataPolicyDomain(0), received.get(0));
	}

	@Test
	@DisplayName("la política sabe si fue aceptada y una versión vieja o vacía no se acepta")
	void domainRules() {
		final AcceptDataPolicyDomain empty = new AcceptDataPolicyDomain(0);
		final AcceptDataPolicyDomain old = new AcceptDataPolicyDomain(1);

		assertFalse(new DataPolicyDomain(1, null, null, null).accepted());
		assertNull(DataPolicyResponseDTO.of(new DataPolicyDomain(1, null, null, null)).acceptedAt());
		assertEquals(MessageKey.DATA_POLICY_VERSION_REQUIRED,
				assertThrows(BookHiveException.class, empty::validate).getUserMessageKey());
		assertEquals(MessageKey.DATA_POLICY_VERSION_OUTDATED,
				assertThrows(BookHiveException.class, () -> old.requireCurrent(2)).getUserMessageKey());
	}

	private DataPolicyController controller() {
		return new DataPolicyController(
				new FindDataPolicyInputAdapter(() -> new DataPolicyDomain(2, "Texto", PUBLISHED, null)),
				new AcceptDataPolicyInputAdapter(acceptance -> {
					received.add(acceptance);
					return new DataPolicyDomain(acceptance.version(), "Texto", PUBLISHED, ACCEPTED);
				}));
	}

}
