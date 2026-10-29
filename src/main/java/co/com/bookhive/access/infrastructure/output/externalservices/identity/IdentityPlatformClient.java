package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class IdentityPlatformClient {

	static final String TOKEN_URL =
			"http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token";

	static final String IDENTITY_TOOLKIT_URL = "https://identitytoolkit.googleapis.com/v1/projects/";

	private static final Duration TOKEN_MARGIN = Duration.ofMinutes(1);

	private final RestClient restClient;
	private final String projectId;

	private final AtomicReference<AccessToken> accessToken =
			new AtomicReference<>(new AccessToken("", Instant.EPOCH));

	@Autowired
	public IdentityPlatformClient(@Value("${bookhive.identity.project-id:}") final String projectId) {
		this(GoogleHttp.builder(), projectId);
	}

	IdentityPlatformClient(final RestClient.Builder restClientBuilder, final String projectId) {
		super();
		this.restClient = restClientBuilder.build();
		this.projectId = TextHelper.getDefaultValue(projectId);
	}

	public String createSessionCookie(final String idToken, final Duration validFor) {

		final Map<?, ?> response = call(
				"createSessionCookie",
				":createSessionCookie",
				Map.of("idToken", TextHelper.getDefaultValue(idToken),
						"validDuration", String.valueOf(validFor.toSeconds())));

		final String sessionCookie = textOf(response, "sessionCookie");

		if (TextHelper.isDefaultValue(sessionCookie)) {
			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_PROVIDER_UNAVAILABLE,
					"Identity Platform respondió sin la cookie de sesión.");
		}

		return sessionCookie;
	}

	public IdentityPlatformAccount lookup(final String identity) {

		final Map<?, ?> response = call(
				"accounts:lookup",
				"/accounts:lookup",
				Map.of("localId", List.of(TextHelper.getDefaultValue(identity))));

		final Object users = ObjectHelper.isNull(response) ? null : response.get("users");

		if (!(users instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof Map<?, ?> user)) {
			return new IdentityPlatformAccount(Long.MAX_VALUE, true);
		}

		return new IdentityPlatformAccount(
				numberOf(user, "validSince"),
				Boolean.parseBoolean(String.valueOf(user.get("disabled"))));
	}

	public void revokeSessions(final String identity) {

		call("accounts:update",
				"/accounts:update",
				Map.of("localId", TextHelper.getDefaultValue(identity),
						"validSince", String.valueOf(Instant.now().getEpochSecond())));
	}

	public boolean isConfigured() {
		return !TextHelper.isDefaultValue(projectId);
	}

	public String getProjectId() {
		return projectId;
	}

	private Map<?, ?> call(final String operation, final String path, final Map<String, ?> body) {

		if (!isConfigured()) {
			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_PROVIDER_UNAVAILABLE,
					"Falta bookhive.identity.project-id: no hay proveedor de identidad configurado (ADR-026).");
		}

		try {
			return restClient.post()
					.uri(IDENTITY_TOOLKIT_URL + projectId + path)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + currentAccessToken())
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.body(Map.class);

		} catch (final HttpClientErrorException.BadRequest exception) {
			throw BookHiveException.create(
					MessageKey.SESSION_NOT_OPENED,
					"Identity Platform rechazó " + operation + " por datos inválidos: "
							+ exception.getResponseBodyAsString());

		} catch (final RestClientException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.SESSION_PROVIDER_UNAVAILABLE,
					"No se pudo completar " + operation + " en Identity Platform.");
		}
	}

	private String currentAccessToken() {

		final AccessToken current = accessToken.get();

		if (current.isValidAt(Instant.now().plus(TOKEN_MARGIN))) {
			return current.value();
		}

		final Map<?, ?> response = restClient.get()
				.uri(TOKEN_URL)
				.header("Metadata-Flavor", "Google")
				.retrieve()
				.body(Map.class);

		final String value = textOf(response, "access_token");

		if (TextHelper.isDefaultValue(value)) {
			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_PROVIDER_UNAVAILABLE,
					"El servidor de metadatos no entregó un token de acceso.");
		}

		accessToken.set(new AccessToken(value, Instant.now().plusSeconds(numberOf(response, "expires_in"))));

		return value;
	}

	private static String textOf(final Map<?, ?> map, final String key) {
		return ObjectHelper.isNull(map) || ObjectHelper.isNull(map.get(key))
				? TextHelper.getDefaultValue()
				: TextHelper.getDefaultValue(String.valueOf(map.get(key)));
	}

	private static long numberOf(final Map<?, ?> map, final String key) {

		final String text = textOf(map, key);

		try {
			return TextHelper.isDefaultValue(text) ? 0L : Long.parseLong(text);
		} catch (final NumberFormatException _) {
			return 0L;
		}
	}

	private record AccessToken(String value, Instant expiresAt) {

		private boolean isValidAt(final Instant moment) {
			return !TextHelper.isDefaultValue(value) && moment.isBefore(expiresAt);
		}

	}

}
