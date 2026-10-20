package co.com.bookhive.gateway.infrastructure.output.identity;

import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.nimbusds.jwt.JWTParser;

import co.com.bookhive.gateway.crosscutting.helper.TextHelper;
import reactor.core.publisher.Mono;

@Component
public class MetadataIdentityTokens {

	static final String IDENTITY_URL =
			"http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity?audience={audience}";
	static final String METADATA_FLAVOR = "Metadata-Flavor";
	static final String GOOGLE = "Google";
	static final Duration RENEW_BEFORE = Duration.ofMinutes(5);
	static final Duration FALLBACK_LIFETIME = Duration.ofMinutes(10);

	private static final String EXPIRES_AT = "exp";

	private final WebClient client;
	private final Clock clock;
	private final AtomicReference<CachedToken> cached =
			new AtomicReference<>(new CachedToken(TextHelper.getDefaultValue(), TextHelper.getDefaultValue(), Instant.EPOCH));

	public MetadataIdentityTokens(final WebClient.Builder clientBuilder, final Clock clock) {
		super();
		this.client = clientBuilder.build();
		this.clock = clock;
	}

	public Mono<String> tokenFor(final String audience) {

		final String cleanAudience = TextHelper.getDefaultValue(audience);
		final CachedToken current = cached.get();

		if (current.audience().equals(cleanAudience) && clock.instant().isBefore(current.renewAt())) {
			return Mono.just(current.value());
		}

		return client.get()
				.uri(IDENTITY_URL, cleanAudience)
				.header(METADATA_FLAVOR, GOOGLE)
				.retrieve()
				.bodyToMono(String.class)
				.map(String::trim)
				.filter(token -> !TextHelper.isDefaultValue(token))
				.doOnNext(token -> cached.set(new CachedToken(cleanAudience, token, renewAt(token, clock.instant()))));
	}

	static Instant renewAt(final String token, final Instant now) {

		try {
			final Object expiresAt = JWTParser.parse(token).getJWTClaimsSet().toJSONObject().get(EXPIRES_AT);

			return expiresAt instanceof Number seconds
					? Instant.ofEpochSecond(seconds.longValue()).minus(RENEW_BEFORE)
					: now.plus(FALLBACK_LIFETIME);

		} catch (final ParseException _) {
			return now.plus(FALLBACK_LIFETIME);
		}
	}

	private record CachedToken(String audience, String value, Instant renewAt) {
	}

}
