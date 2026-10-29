package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

@Component
public class GoogleSessionKeys implements JWKSource<SecurityContext> {

	static final String KEYS_URL = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/publicKeys";

	private static final Duration KEEP_FOR = Duration.ofHours(1);

	private final RestClient restClient;

	private final AtomicReference<CachedKeys> cached =
			new AtomicReference<>(new CachedKeys(new JWKSet(), Instant.EPOCH));

	@Autowired
	public GoogleSessionKeys() {
		this(GoogleHttp.builder());
	}

	GoogleSessionKeys(final RestClient.Builder restClientBuilder) {
		super();
		this.restClient = restClientBuilder.build();
	}

	@Override
	public List<JWK> get(final JWKSelector selector, final SecurityContext context) throws KeySourceException {

		final List<JWK> found = selector.select(currentKeys());

		if (found.isEmpty()) {
			return selector.select(reload());
		}

		return found;
	}

	private JWKSet currentKeys() throws KeySourceException {

		final CachedKeys current = cached.get();

		if (Instant.now().isBefore(current.loadedAt().plus(KEEP_FOR))) {
			return current.keys();
		}

		return reload();
	}

	private JWKSet reload() throws KeySourceException {

		final Map<?, ?> certificates;

		try {
			certificates = restClient.get().uri(KEYS_URL).retrieve().body(Map.class);
		} catch (final RestClientException exception) {
			throw new KeySourceException("No se pudieron descargar las llaves de las cookies de sesión.", exception);
		}

		final JWKSet keys = toKeySet(certificates);
		cached.set(new CachedKeys(keys, Instant.now()));

		return keys;
	}

	static JWKSet toKeySet(final Map<?, ?> certificates) throws KeySourceException {

		if (ObjectHelper.isNull(certificates)) {
			return new JWKSet();
		}

		try {
			final List<JWK> keys = certificates.entrySet().stream()
					.<JWK>map(entry -> toKey(String.valueOf(entry.getKey()), String.valueOf(entry.getValue())))
					.toList();

			return new JWKSet(keys);

		} catch (final IllegalArgumentException exception) {
			throw new KeySourceException(exception.getMessage(), exception);
		}
	}

	private static JWK toKey(final String keyId, final String pem) {

		try {
			final X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
					.generateCertificate(new ByteArrayInputStream(pem.getBytes(StandardCharsets.US_ASCII)));

			return new RSAKey.Builder(RSAKey.parse(certificate)).keyID(keyId).build();

		} catch (final CertificateException | JOSEException | ClassCastException exception) {
			throw new IllegalArgumentException("El certificado " + keyId + " de Google no se pudo leer.", exception);
		}
	}

	private record CachedKeys(JWKSet keys, Instant loadedAt) {
	}

}
