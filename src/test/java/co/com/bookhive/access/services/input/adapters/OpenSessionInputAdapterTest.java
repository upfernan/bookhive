package co.com.bookhive.access.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.services.input.dto.request.OpenSessionRequestDTO;

class OpenSessionInputAdapterTest {

	private final List<String> receivedIdTokens = new ArrayList<>();

	private final OpenSessionInputAdapter adapter = new OpenSessionInputAdapter(session -> {
		receivedIdTokens.add(session.idToken());
		return session.withSessionToken("cookie-de-sesion");
	});

	@Test
	@DisplayName("convierte la petición en dominio y devuelve la cookie que abrió el caso de uso")
	void returnsOpenedSession() {
		assertEquals("cookie-de-sesion", adapter.execute(new OpenSessionRequestDTO(" pase ")).sessionToken());
		assertEquals(List.of("pase"), receivedIdTokens);
	}

	@Test
	@DisplayName("si no llega petición le pasa al caso de uso un pase vacío")
	void handlesMissingRequest() {
		adapter.execute(null);

		assertEquals(List.of(""), receivedIdTokens);
	}

}
