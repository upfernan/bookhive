package co.com.bookhive.access.infrastructure.input.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import jakarta.servlet.http.Cookie;

class SessionCookieTokenResolverTest {

	private final SessionCookieTokenResolver resolver = new SessionCookieTokenResolver();

	@Test
	@DisplayName("toma el pase de la cookie de sesión")
	void readsSessionCookie() {
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(new Cookie("otra", "x"), new Cookie(SessionCookieTokenResolver.SESSION_COOKIE, " pase "));

		assertEquals("pase", resolver.resolve(request));
	}

	@Test
	@DisplayName("sin cookies no hay pase")
	void withoutCookiesThereIsNoToken() {
		assertNull(resolver.resolve(new MockHttpServletRequest()));
	}

	@Test
	@DisplayName("una cookie de sesión vacía no cuenta como pase")
	void blankSessionCookieIsIgnored() {
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(new Cookie(SessionCookieTokenResolver.SESSION_COOKIE, "  "));

		assertNull(resolver.resolve(request));
	}

	@Test
	@DisplayName("otras cookies no cuentan como pase")
	void otherCookiesAreIgnored() {
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(new Cookie("preferencias", "oscuro"));

		assertNull(resolver.resolve(request));
	}

}
