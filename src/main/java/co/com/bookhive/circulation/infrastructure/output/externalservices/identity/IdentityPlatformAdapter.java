package co.com.bookhive.circulation.infrastructure.output.externalservices.identity;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.services.output.ports.IdentityProviderPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Component
public class IdentityPlatformAdapter implements IdentityProviderPort {

	private static final Logger logger = LoggerFactory.getLogger(IdentityPlatformAdapter.class);

	private final IdentityPlatformClient identityPlatform;
	private final JwtDecoder sessionDecoder;
	private final Duration sessionLength;

	public IdentityPlatformAdapter(
			final IdentityPlatformClient identityPlatform,
			final JwtDecoder sessionDecoder,
			@Value("${bookhive.session.minutes:20}") final long sessionMinutes) {

		super();
		this.identityPlatform = identityPlatform;
		this.sessionDecoder = sessionDecoder;
		this.sessionLength = Duration.ofMinutes(sessionMinutes);
	}

	@Override
	public String openSession(final String idToken) {
		return identityPlatform.createSessionCookie(idToken, sessionLength);
	}

	@Override
	public String identityOf(final String sessionToken) {

		try {
			return sessionDecoder.decode(sessionToken).getSubject();
		} catch (final JwtException exception) {
			throw BookHiveException.createUnexpected(
					exception,
					MessageKey.SESSION_NOT_OPENED,
					"La cookie de sesión recién creada no se pudo validar.");
		}
	}

	@Override
	public void closeSession(final String sessionToken) {

		final String identity;

		try {
			identity = sessionDecoder.decode(sessionToken).getSubject();
		} catch (final JwtException _) {
			logger.info("Se pidió cerrar una sesión que ya no estaba vigente.");
			return;
		}

		identityPlatform.revokeSessions(identity);
	}

}
