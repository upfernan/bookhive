package co.com.bookhive.access.services.usecase.session.closesession.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.domain.session.closesession.CloseSessionDomain;
import co.com.bookhive.access.testdata.FakeAccess;

class CloseSessionUseCaseImplTest {

	private final FakeAccess.IdentityProvider provider = new FakeAccess.IdentityProvider("uid");
	private final CloseSessionUseCaseImpl useCase = new CloseSessionUseCaseImpl(provider);

	@Test
	@DisplayName("le pide al proveedor cerrar la sesión de la cookie")
	void closesSession() {
		useCase.execute(new CloseSessionDomain(" cookie-de-sesion "));

		assertEquals(List.of("cookie-de-sesion"), provider.closedSessions());
	}

	@Test
	@DisplayName("si no llega cookie no hay nada que cerrar")
	void ignoresMissingCookie() {
		useCase.execute(new CloseSessionDomain("   "));
		useCase.execute(null);

		assertTrue(provider.closedSessions().isEmpty());
	}

}
