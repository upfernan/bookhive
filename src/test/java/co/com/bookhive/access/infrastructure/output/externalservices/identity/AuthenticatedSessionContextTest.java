package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class AuthenticatedSessionContextTest {

	private static final String IDENTITY = "uid-bibliotecario";
	private static final UUID PERSON = UUIDHelper.generateNew();
	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	private final List<String> searchedIdentities = new ArrayList<>();

	private final AuthenticatedSessionContext context = new AuthenticatedSessionContext(identity -> {
		searchedIdentities.add(identity);
		final PersonEntity person = new PersonEntity();
		person.setId(PERSON);
		person.setInstitution(INSTITUTION);
		return person;
	});

	@AfterEach
	void clean() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("la institución y el bibliotecario salen de la persona dueña del pase")
	void readsPersonOfAuthenticatedPass() {
		SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(pass()));

		assertEquals(INSTITUTION, context.getInstitution());
		assertEquals(PERSON, context.getLibrarian());
		assertEquals(List.of(IDENTITY, IDENTITY), searchedIdentities);
	}

	@Test
	@DisplayName("sin una petición autenticada no hay sesión que leer")
	void rejectsMissingAuthentication() {
		final BookHiveException exception = assertThrows(BookHiveException.class, context::getInstitution);

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("una autenticación que no trae pase tampoco sirve")
	void rejectsAuthenticationWithoutPass() {
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("alguien", "clave"));

		final BookHiveException exception = assertThrows(BookHiveException.class, context::getLibrarian);

		assertEquals(MessageKey.SESSION_NOT_RESOLVED, exception.getUserMessageKey());
	}

	private static Jwt pass() {
		final Instant now = Instant.now();
		return new Jwt("pase", now, now.plusSeconds(60), Map.of("alg", "RS256"), Map.of("sub", IDENTITY));
	}

}
