package co.com.bookhive.circulation.infrastructure.output.externalservices.identity;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.initializer.tenancy.PersonDirectory;

@Service
@ConditionalOnProperty(
		name = "bookhive.session.provisional.enabled",
		havingValue = "false",
		matchIfMissing = true)
public class AuthenticatedSessionContext implements SessionContextPort {

	private final PersonDirectory personDirectory;

	public AuthenticatedSessionContext(final PersonDirectory personDirectory) {
		super();
		this.personDirectory = personDirectory;
	}

	@Override
	public UUID getInstitution() {
		return personDirectory.findActive(authenticatedIdentity()).institution();
	}

	@Override
	public UUID getLibrarian() {
		return personDirectory.findActive(authenticatedIdentity()).id();
	}

	private static String authenticatedIdentity() {

		final Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();

		if (ObjectHelper.isNull(authentication)
				|| !(authentication.getPrincipal() instanceof Jwt pass)) {

			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_NOT_RESOLVED,
					"Se pidió el contexto de sesión sin una petición autenticada detrás.");
		}

		return pass.getSubject();
	}

}
