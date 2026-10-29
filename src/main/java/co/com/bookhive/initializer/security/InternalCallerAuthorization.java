package co.com.bookhive.initializer.security;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import co.com.bookhive.crosscutting.helper.TextHelper;

public final class InternalCallerAuthorization implements AuthorizationManager<RequestAuthorizationContext> {

	static final String GATEWAY_IDENTITY_HEADER = "X-Serverless-Authorization";

	private static final Logger logger = LoggerFactory.getLogger(InternalCallerAuthorization.class);

	@Override
	public AuthorizationResult authorize(
			final Supplier<? extends Authentication> authentication,
			final RequestAuthorizationContext context) {

		final String gatewayIdentity = TextHelper.getDefaultValue(context.getRequest().getHeader(GATEWAY_IDENTITY_HEADER));
		final boolean direct = TextHelper.isDefaultValue(gatewayIdentity);

		if (!direct) {
			final String path = context.getRequest().getRequestURI();
			logger.warn("Se rechazó la llamada interna a {}: llegó por la puerta y no directo desde Google.", path);
		}

		return new AuthorizationDecision(direct);
	}

}
