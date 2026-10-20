package co.com.bookhive.gateway.infrastructure.output.identity;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import co.com.bookhive.gateway.crosscutting.helper.TextHelper;
import reactor.core.publisher.Mono;

@Component
public class GoogleSessionKeys implements SessionKeys {

	static final String KEYS_URL = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/publicKeys";

	static final Duration KEEP_FOR = Duration.ofHours(1);
	static final Duration MIN_RELOAD_GAP = Duration.ofMinutes(1);

	private static final Logger logger = LoggerFactory.getLogger(GoogleSessionKeys.class);
	private static final ParameterizedTypeReference<Map<String, String>> CERTIFICATES =
			new ParameterizedTypeReference<>() {
			};

	private final WebClient client;
	private final Clock clock;
	private final AtomicReference<CachedKeys> cached = new AtomicReference<>(new CachedKeys(Map.of(), Instant.EPOCH));

	public GoogleSessionKeys(final WebClient.Builder clientBuilder, final Clock clock) {
		super();
		this.client = clientBuilder.build();
		this.clock = clock;
	}

	@Override
	public Mono<RSAPublicKey> find(final String keyId) {

		final String cleanKeyId = TextHelper.getDefaultValue(keyId);
		final CachedKeys current = cached.get();
		final Instant now = clock.instant();
		final RSAPublicKey known = current.keys().get(cleanKeyId);

		if (known != null && now.isBefore(current.loadedAt().plus(KEEP_FOR))) {
			return Mono.just(known);
		}

		if (now.isBefore(current.loadedAt().plus(MIN_RELOAD_GAP))) {
			return Mono.justOrEmpty(known);
		}

		return reload().mapNotNull(keys -> keys.get(cleanKeyId));
	}

	private Mono<Map<String, RSAPublicKey>> reload() {
		return client.get()
				.uri(KEYS_URL)
				.retrieve()
				.bodyToMono(CERTIFICATES)
				.map(GoogleSessionKeys::toKeys)
				.doOnNext(keys -> cached.set(new CachedKeys(keys, clock.instant())))
				.doOnError(failure -> logger.warn("No se pudieron descargar las llaves de las cookies de sesión: {}",
						failure.getMessage()));
	}

	static Map<String, RSAPublicKey> toKeys(final Map<String, String> certificates) {

		final Map<String, RSAPublicKey> keys = new HashMap<>();

		if (certificates == null) {
			return keys;
		}

		certificates.forEach((keyId, pem) -> publicKeyOf(TextHelper.getDefaultValue(pem))
				.filter(RSAPublicKey.class::isInstance)
				.map(RSAPublicKey.class::cast)
				.ifPresent(rsaKey -> keys.put(TextHelper.getDefaultValue(keyId), rsaKey)));

		return Map.copyOf(keys);
	}

	private static Optional<PublicKey> publicKeyOf(final String pem) {

		try {
			return Optional.of(CertificateFactory.getInstance("X.509")
					.generateCertificate(new ByteArrayInputStream(pem.getBytes(StandardCharsets.US_ASCII)))
					.getPublicKey());

		} catch (final CertificateException exception) {
			logger.warn("Google entregó un certificado de sesión que no se pudo leer: {}", exception.getMessage());
			return Optional.empty();
		}
	}

	private record CachedKeys(Map<String, RSAPublicKey> keys, Instant loadedAt) {
	}

}
