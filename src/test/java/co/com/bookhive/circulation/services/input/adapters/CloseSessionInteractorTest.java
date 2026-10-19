package co.com.bookhive.circulation.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.output.ports.IdentityProviderPort;

class CloseSessionInteractorTest {

	private List<String> closedSessions;
	private CloseSessionInteractor interactor;

	@BeforeEach
	void prepare() {
		closedSessions = new ArrayList<>();
		interactor = new CloseSessionInteractor(new IdentityProviderPort() {

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
		interactor.execute(" cookie-de-sesion ");

		assertEquals(List.of("cookie-de-sesion"), closedSessions);
	}

	@Test
	@DisplayName("si no llega cookie no hay nada que cerrar")
	void ignoresMissingCookie() {
		interactor.execute(null);
		interactor.execute("   ");

		assertTrue(closedSessions.isEmpty());
	}

}
