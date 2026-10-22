package co.com.bookhive.shared.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.shared.services.input.dto.request.ResolveMessageRequestDTO;

class ResolveMessageInputAdapterTest {

	private final ResolveMessageInputAdapter adapter =
			new ResolveMessageInputAdapter(message -> message.withText("Texto de " + message.getKey()));

	@Test
	@DisplayName("convierte la petición en dominio y devuelve el texto que resolvió el caso de uso")
	void returnsResolvedText() {
		assertEquals("Texto de book-copy.not-available",
				adapter.execute(new ResolveMessageRequestDTO("book-copy.not-available")).text());
	}

	@Test
	@DisplayName("si no llega petición pregunta por la clave vacía")
	void handlesMissingRequest() {
		final ResolveMessageInputAdapter general =
				new ResolveMessageInputAdapter(message -> message.withText("Mensaje general" + message.getKey()));

		assertEquals("Mensaje general", general.execute(null).text());
	}

}
