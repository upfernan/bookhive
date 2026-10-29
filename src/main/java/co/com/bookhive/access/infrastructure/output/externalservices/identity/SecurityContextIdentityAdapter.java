package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import co.com.bookhive.access.services.output.ports.SessionIdentityPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

@Service
@Primary
@ConditionalOnProperty(
		name = "bookhive.session.provisional.enabled",
		havingValue = "false",
		matchIfMissing = true)
public class SecurityContextIdentityAdapter implements SessionIdentityPort {

	@Override
	public String getIdentity() {

		final Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();

		if (ObjectHelper.isNull(authentication)
				|| !(authentication.getPrincipal() instanceof Jwt pass)) {

			throw BookHiveException.createUnexpected(
					MessageKey.SESSION_NOT_RESOLVED,
					"Se pidió la identidad de la sesión sin una petición autenticada detrás.");
		}

		return pass.getSubject();
	}

}
