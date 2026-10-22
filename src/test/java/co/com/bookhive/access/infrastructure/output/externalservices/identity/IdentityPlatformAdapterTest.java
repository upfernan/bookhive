package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class IdentityPlatformAdapterTest {

	private static final String PROJECT = "bookhive-prueba";
	private static final String TOOLKIT = IdentityPlatformClient.IDENTITY_TOOLKIT_URL + PROJECT;

	private static final JwtDecoder VALID_SESSIONS = token -> Jwt.withTokenValue(token)
			.header("alg", "RS256")
			.subject("uid-de-" + token)
			.build();

	private static final JwtDecoder CLOSED_SESSIONS = token -> {
		throw new BadJwtException("La sesión ya no está vigente.");
	};

	private MockRestServiceServer google;
	private IdentityPlatformClient client;

	@BeforeEach
	void prepare() {
		final RestClient.Builder builder = RestClient.builder();
		google = MockRestServiceServer.bindTo(builder).build();
		client = new IdentityPlatformClient(builder, PROJECT);
	}

	@Test
	@DisplayName("abre la sesión con la duración configurada")
	void opensSessionWithConfiguredLength() {
		expectAccessToken();
		google.expect(requestTo(TOOLKIT + ":createSessionCookie"))
				.andExpect(jsonPath("$.validDuration").value("900"))
				.andRespond(json("{\"sessionCookie\":\"cookie\"}"));

		assertEquals("cookie", new IdentityPlatformAdapter(client, VALID_SESSIONS, 15).openSession("pase"));
		google.verify();
	}

	@Test
	@DisplayName("la identidad de la sesión es el identificador de la persona en el proveedor")
	void identityIsSubject() {
		assertEquals("uid-de-cookie", new IdentityPlatformAdapter(client, VALID_SESSIONS, 20).identityOf("cookie"));
	}

	@Test
	@DisplayName("si la sesión recién creada no es válida no se abre")
	void invalidNewSessionIsRejected() {
		final IdentityPlatformAdapter adapter = new IdentityPlatformAdapter(client, CLOSED_SESSIONS, 20);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> adapter.identityOf("cookie"));

		assertEquals(MessageKey.SESSION_NOT_OPENED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("al cerrar la sesión la invalida en Google para que deje de servir de inmediato")
	void closingRevokesSessions() {
		expectAccessToken();
		google.expect(requestTo(TOOLKIT + "/accounts:update"))
				.andExpect(jsonPath("$.localId").value("uid-de-cookie"))
				.andRespond(json("{}"));

		new IdentityPlatformAdapter(client, VALID_SESSIONS, 20).closeSession("cookie");

		google.verify();
	}

	@Test
	@DisplayName("cerrar una sesión vencida no llama a Google")
	void closingExpiredSessionDoesNothing() {
		new IdentityPlatformAdapter(client, CLOSED_SESSIONS, 20).closeSession("cookie");

		google.verify();
	}

	private void expectAccessToken() {
		google.expect(requestTo(IdentityPlatformClient.TOKEN_URL))
				.andRespond(json("{\"access_token\":\"acceso\",\"expires_in\":3599}"));
	}

	private static ResponseCreator json(final String body) {
		return withSuccess(body, MediaType.APPLICATION_JSON);
	}

}
