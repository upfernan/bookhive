package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class SecurityContextIdentityAdapterTest {

	private final SecurityContextIdentityAdapter adapter = new SecurityContextIdentityAdapter();

	@AfterEach
	void clean() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("la identidad es el dueño del pase con que llegó la petición")
	void readsOwnerOfPass() {
		final Instant now = Instant.now();
		SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
				new Jwt("pase", now, now.plusSeconds(60), Map.of("alg", "RS256"), Map.of("sub", "uid-bibliotecario"))));

		assertEquals("uid-bibliotecario", adapter.getIdentity());
	}

	@Test
	@DisplayName("sin una petición autenticada no hay identidad que leer")
	void rejectsMissingAuthentication() {
		final BookHiveException exception = assertThrows(BookHiveException.class, adapter::getIdentity);

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("una autenticación que no trae pase tampoco sirve")
	void rejectsAuthenticationWithoutPass() {
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("alguien", "clave"));

		assertThrows(BookHiveException.class, adapter::getIdentity);
	}

}
