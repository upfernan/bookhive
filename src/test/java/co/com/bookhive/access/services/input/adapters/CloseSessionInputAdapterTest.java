package co.com.bookhive.access.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.services.output.ports.IdentityProviderPort;

class CloseSessionInputAdapterTest {

	private List<String> closedSessions;
	private CloseSessionInputAdapter inputAdapter;

	@BeforeEach
	void prepare() {
		closedSessions = new ArrayList<>();
		inputAdapter = new CloseSessionInputAdapter(new IdentityProviderPort() {

			@Override
			public String openSession(final String idToken) {
				throw new UnsupportedOperationException("Cerrar una sesión no abre otra.");
			}

			@Override
			public String identityOf(final String sessionToken) {
				throw new UnsupportedOperationException("Cerrar una sesión no consulta la identidad.");
			}

			@Override
			public void closeSession(final String sessionToken) {
				closedSessions.add(sessionToken);
			}

		});
	}

	@Test
	@DisplayName("le pide al proveedor cerrar la sesión de la cookie")
	void closesSession() {
		inputAdapter.execute(" cookie-de-sesion ");

		assertEquals(List.of("cookie-de-sesion"), closedSessions);
	}

	@Test
	@DisplayName("si no llega cookie no hay nada que cerrar")
	void ignoresMissingCookie() {
		inputAdapter.execute(null);
		inputAdapter.execute("   ");

		assertTrue(closedSessions.isEmpty());
	}

}
