package co.com.bookhive.initializer.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

class InternalCallerAuthorizationTest {

	private final InternalCallerAuthorization authorization = new InternalCallerAuthorization();

	@Test
	@DisplayName("deja pasar la llamada que llega directo de Google, ya revisada por Cloud Run")
	void allowsDirectCalls() {
		final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/tareas/prestamos");

		assertTrue(authorization.authorize(() -> null, new RequestAuthorizationContext(request)).isGranted());
	}

	@Test
	@DisplayName("rechaza la llamada que llega por la puerta, aunque traiga sesión")
	void rejectsCallsThroughTheGateway() {
		final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/avisos/correo");
		request.addHeader(InternalCallerAuthorization.GATEWAY_IDENTITY_HEADER, "Bearer pase-de-la-puerta");

		assertFalse(authorization.authorize(() -> null, new RequestAuthorizationContext(request)).isGranted());
	}

}
