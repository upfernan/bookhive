package co.com.bookhive.access.services.usecase.session.changeperson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.com.bookhive.access.domain.session.changeperson.PersonChangeDomain;
import co.com.bookhive.access.infrastructure.input.controllers.PublicDataPolicyController;
import co.com.bookhive.access.infrastructure.input.controllers.RegistrationController;
import co.com.bookhive.access.services.input.adapters.ChangePersonInputAdapter;
import co.com.bookhive.access.services.input.dto.request.ChangePersonRequestDTO;
import co.com.bookhive.access.services.input.dto.request.RegisterPersonRequestDTO;
import co.com.bookhive.access.services.input.dto.response.PublicDataPolicyResponseDTO;
import co.com.bookhive.access.services.input.dto.response.RegisterPersonResponseDTO;
import co.com.bookhive.access.services.output.ports.repositories.PersonStatusRepository;
import co.com.bookhive.access.services.usecase.session.changeperson.impl.ChangePersonUseCaseImpl;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class ChangePersonTest {

	private static final UUID PERSON = UUID.randomUUID();

	private final List<String> changes = new ArrayList<>();
	private final ChangePersonInputAdapter adapter = new ChangePersonInputAdapter(new ChangePersonUseCaseImpl(new PersonStatusRepository() {

		@Override
		public void changeStatus(final UUID person, final String schema, final String status) {
			changes.add(person + " " + schema + " " + status);
		}

		@Override
		public void anonymize(final UUID person, final String schema) {
			changes.add(person + " " + schema + " ANONIMA");
		}

	}));

	@Test
	@DisplayName("da de baja, reactiva o anonimiza la persona en la institución indicada")
	void changesPerson() {
		adapter.execute(new ChangePersonRequestDTO(PERSON, " uco ", ChangePersonRequestDTO.DEACTIVATE));
		adapter.execute(new ChangePersonRequestDTO(PERSON, "uco", ChangePersonRequestDTO.REACTIVATE));
		adapter.execute(new ChangePersonRequestDTO(PERSON, "uco", ChangePersonRequestDTO.ANONYMIZE));
		adapter.execute(new ChangePersonRequestDTO(PERSON, "uco", "OTRA"));

		assertEquals(List.of(PERSON + " uco INACTIVA", PERSON + " uco ACTIVA", PERSON + " uco ANONIMA", PERSON + " uco INACTIVA"), changes);
	}

	@Test
	@DisplayName("sin persona o sin institución es un error inesperado")
	void requiresPersonAndSchema() {
		final ChangePersonRequestDTO withoutSchema = new ChangePersonRequestDTO(PERSON, " ", ChangePersonRequestDTO.DEACTIVATE);

		assertEquals(BookHiveException.Kind.UNEXPECTED, assertThrows(BookHiveException.class, () -> adapter.execute(null)).getKind());
		assertThrows(BookHiveException.class, () -> adapter.execute(withoutSchema));
		assertEquals(PersonChangeDomain.Action.DEACTIVATE, new PersonChangeDomain(null, null, null).action());
		assertTrue(changes.isEmpty());
	}

	@Test
	@DisplayName("el registro responde 201 con la cookie de sesión y el aviso público responde con su contenido")
	void controllers() {
		final ResponseEntity<Void> registered = new RegistrationController(
				request -> new RegisterPersonResponseDTO(PERSON, "cookie-" + request.name()), 20)
				.register(new RegisterPersonRequestDTO("token", "carla", 1));
		final PublicDataPolicyResponseDTO policy = new PublicDataPolicyResponseDTO(1, "Aviso", Instant.EPOCH);
		final ResponseEntity<PublicDataPolicyResponseDTO> found =
				new PublicDataPolicyController(domain -> domain.equals("uco.edu.co") ? policy : null).find("uco.edu.co");

		assertEquals(HttpStatus.CREATED, registered.getStatusCode());
		final String cookie = registered.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		assertTrue(cookie.contains("cookie-carla") && cookie.contains("HttpOnly") && cookie.contains("Secure"));
		assertEquals(policy, found.getBody());
	}

}
