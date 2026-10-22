package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import co.com.bookhive.access.services.output.ports.SessionContextPort;
import co.com.bookhive.access.services.output.ports.repositories.PersonRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Service
@ConditionalOnProperty(
		name = "bookhive.session.provisional.enabled",
		havingValue = "false",
		matchIfMissing = true)
public class AuthenticatedSessionContext implements SessionContextPort {

	private final PersonRepository personRepository;

	public AuthenticatedSessionContext(final PersonRepository personRepository) {
		super();
		this.personRepository = personRepository;
	}

	@Override
	public UUID getInstitution() {
		return personRepository.findActive(authenticatedIdentity()).getInstitution();
	}

	@Override
	public UUID getLibrarian() {
		return personRepository.findActive(authenticatedIdentity()).getId();
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
