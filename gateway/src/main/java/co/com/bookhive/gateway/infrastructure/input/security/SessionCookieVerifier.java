package co.com.bookhive.gateway.infrastructure.input.security;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import co.com.bookhive.gateway.crosscutting.helper.TextHelper;
import co.com.bookhive.gateway.infrastructure.output.identity.SessionKeys;
import reactor.core.publisher.Mono;

@Component
public class SessionCookieVerifier {

	public static final String SESSION_COOKIE = "bookhive_session";

	static final String ISSUER_PREFIX = "https://session.firebase.google.com/";
	static final Duration CLOCK_SKEW = Duration.ofMinutes(1);

	private static final int MAX_ACCOUNT_LENGTH = 128;
	private static final String EXPIRES_AT = "exp";
	private static final String ISSUED_AT = "iat";

	private final String projectId;
	private final SessionKeys keys;
	private final Clock clock;

	public SessionCookieVerifier(
			@Value("${bookhive.identity.project-id:}") final String projectId,
			final SessionKeys keys,
			final Clock clock) {

		super();
		this.projectId = TextHelper.getDefaultValue(projectId);
		this.keys = keys;
		this.clock = clock;
	}

	public Mono<String> accountOf(final ServerHttpRequest request) {

		final HttpCookie cookie = request.getCookies().getFirst(SESSION_COOKIE);

		return accountOf(cookie == null ? TextHelper.getDefaultValue() : cookie.getValue());
	}

	Mono<String> accountOf(final String token) {

		final String cleanToken = TextHelper.getDefaultValue(token);

		if (TextHelper.isDefaultValue(projectId) || TextHelper.isDefaultValue(cleanToken)) {
			return Mono.just(TextHelper.getDefaultValue());
		}

		final SignedJWT session;

		try {
			session = SignedJWT.parse(cleanToken);
		} catch (final ParseException _) {
			return Mono.just(TextHelper.getDefaultValue());
		}

		if (!JWSAlgorithm.RS256.equals(session.getHeader().getAlgorithm())) {
			return Mono.just(TextHelper.getDefaultValue());
		}

		return keys.find(session.getHeader().getKeyID())
				.map(key -> accountIfValid(session, key))
				.defaultIfEmpty(TextHelper.getDefaultValue())
				.onErrorReturn(TextHelper.getDefaultValue());
	}

	private String accountIfValid(final SignedJWT session, final RSAPublicKey key) {

		try {
			if (!session.verify(new RSASSAVerifier(key))) {
				return TextHelper.getDefaultValue();
			}

			final JWTClaimsSet claims = session.getJWTClaimsSet();

			return hasValidClaims(claims, clock.instant())
					? TextHelper.getDefaultValue(claims.getSubject())
					: TextHelper.getDefaultValue();

		} catch (final JOSEException | ParseException _) {
			return TextHelper.getDefaultValue();
		}
	}

	private boolean hasValidClaims(final JWTClaimsSet claims, final Instant now) {

		final List<String> audience = claims.getAudience();
		final String subject = TextHelper.getDefaultValue(claims.getSubject());
		final Map<String, Object> values = claims.toJSONObject();

		return (ISSUER_PREFIX + projectId).equals(claims.getIssuer())
				&& audience != null && audience.contains(projectId)
				&& !TextHelper.isDefaultValue(subject) && subject.length() <= MAX_ACCOUNT_LENGTH
				&& notExpired(values.get(EXPIRES_AT), now)
				&& notIssuedInFuture(values.get(ISSUED_AT), now);
	}

	private static boolean notExpired(final Object expiresAt, final Instant now) {
		return instantOf(expiresAt).map(moment -> moment.plus(CLOCK_SKEW).isAfter(now)).orElse(false);
	}

	private static boolean notIssuedInFuture(final Object issuedAt, final Instant now) {
		return instantOf(issuedAt).map(moment -> !moment.minus(CLOCK_SKEW).isAfter(now)).orElse(true);
	}

	static Optional<Instant> instantOf(final Object secondsSinceEpoch) {
		return secondsSinceEpoch instanceof Number seconds
				? Optional.of(Instant.ofEpochSecond(seconds.longValue()))
				: Optional.empty();
	}

}
