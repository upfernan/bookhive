package co.com.bookhive.circulation.infrastructure.output.externalservices.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class IdentityPlatformClientTest {

	private static final String PROJECT = "bookhive-prueba";
	private static final String TOOLKIT = IdentityPlatformClient.IDENTITY_TOOLKIT_URL + PROJECT;
	private static final String ACCESS = "acceso-de-prueba";

	private MockRestServiceServer google;
	private IdentityPlatformClient client;

	@BeforeEach
	void prepare() {
		final RestClient.Builder builder = RestClient.builder();
		google = MockRestServiceServer.bindTo(builder).build();
		client = new IdentityPlatformClient(builder, PROJECT);
	}

	@Test
	@DisplayName("crea la cookie de sesión con el pase y la duración pedida")
	void createsSessionCookie() {
		expectAccessToken(ExpectedCount.once());
		google.expect(requestTo(TOOLKIT + ":createSessionCookie"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("Authorization", "Bearer " + ACCESS))
				.andExpect(jsonPath("$.idToken").value("pase"))
				.andExpect(jsonPath("$.validDuration").value("1200"))
				.andRespond(json("{\"sessionCookie\":\"cookie-de-sesion\"}"));

		assertEquals("cookie-de-sesion", client.createSessionCookie("pase", Duration.ofMinutes(20)));
		google.verify();
	}

	@Test
	@DisplayName("reutiliza el token de acceso mientras no se venza")
	void reusesAccessToken() {
		expectAccessToken(ExpectedCount.once());
		google.expect(ExpectedCount.twice(), requestTo(TOOLKIT + "/accounts:update"))
				.andExpect(jsonPath("$.localId").value("uid"))
				.andRespond(json("{}"));

		client.revokeSessions("uid");
		client.revokeSessions("uid");

		google.verify();
	}

	@Test
	@DisplayName("lee desde cuándo valen las sesiones y si la cuenta está activa")
	void looksUpAccount() {
		expectAccessToken(ExpectedCount.once());
		google.expect(requestTo(TOOLKIT + "/accounts:lookup"))
				.andExpect(jsonPath("$.localId[0]").value("uid"))
				.andRespond(json("{\"users\":[{\"localId\":\"uid\",\"validSince\":\"1700000000\",\"disabled\":false}]}"));

		final IdentityPlatformAccount account = client.lookup("uid");

		assertEquals(new IdentityPlatformAccount(1_700_000_000L, false), account);
	}

	@Test
	@DisplayName("una cuenta que no existe se trata como deshabilitada")
	void unknownAccountIsDisabled() {
		expectAccessToken(ExpectedCount.once());
		google.expect(requestTo(TOOLKIT + "/accounts:lookup")).andRespond(json("{}"));

		assertTrue(client.lookup("uid").disabled());
	}

	@Test
	@DisplayName("un pase inválido se rechaza como dato inválido")
	void badRequestMeansSessionNotOpened() {
		expectAccessToken(ExpectedCount.once());
		google.expect(requestTo(TOOLKIT + ":createSessionCookie")).andRespond(withBadRequest());
		final Duration length = Duration.ofMinutes(20);

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> client.createSessionCookie("pase-vencido", length));

		assertEquals(MessageKey.SESSION_NOT_OPENED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.MISSING_DATA, exception.getKind());
	}

	@Test
	@DisplayName("si Google falla se informa que el servicio no está disponible")
	void serverErrorMeansProviderUnavailable() {
		expectAccessToken(ExpectedCount.once());
		google.expect(requestTo(TOOLKIT + "/accounts:update")).andRespond(withServerError());

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> client.revokeSessions("uid"));

		assertEquals(MessageKey.SESSION_PROVIDER_UNAVAILABLE, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
	}

	@Test
	@DisplayName("si Google no entrega la cookie se trata como falla del proveedor")
	void missingCookieIsUnexpected() {
		expectAccessToken(ExpectedCount.once());
		google.expect(requestTo(TOOLKIT + ":createSessionCookie")).andRespond(json("{}"));
		final Duration length = Duration.ofMinutes(20);

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> client.createSessionCookie("pase", length));

		assertEquals(MessageKey.SESSION_PROVIDER_UNAVAILABLE, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("si el servidor de metadatos no entrega token no se llama a Identity Platform")
	void missingAccessTokenIsUnexpected() {
		google.expect(requestTo(IdentityPlatformClient.TOKEN_URL))
				.andExpect(header("Metadata-Flavor", "Google"))
				.andRespond(json("{\"expires_in\":\"no-es-numero\"}"));

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> client.lookup("uid"));

		assertEquals(MessageKey.SESSION_PROVIDER_UNAVAILABLE, exception.getUserMessageKey());
		google.verify();
	}

	@Test
	@DisplayName("sin proyecto configurado no hay proveedor de identidad")
	void requiresProject() {
		final IdentityPlatformClient unconfigured = new IdentityPlatformClient(RestClient.builder(), "  ");

		assertFalse(unconfigured.isConfigured());
		assertEquals("", unconfigured.getProjectId());
		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> unconfigured.revokeSessions("uid"));
		assertEquals(MessageKey.SESSION_PROVIDER_UNAVAILABLE, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("con proyecto configurado lo informa")
	void reportsProject() {
		assertTrue(client.isConfigured());
		assertEquals(PROJECT, client.getProjectId());
	}

	private void expectAccessToken(final ExpectedCount count) {
		google.expect(count, requestTo(IdentityPlatformClient.TOKEN_URL))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header("Metadata-Flavor", "Google"))
				.andRespond(json("{\"access_token\":\"" + ACCESS + "\",\"expires_in\":3599}"));
	}

	private static ResponseCreator json(final String body) {
		return withSuccess(body, MediaType.APPLICATION_JSON);
	}

}
