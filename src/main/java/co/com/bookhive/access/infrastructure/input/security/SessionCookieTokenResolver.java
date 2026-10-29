package co.com.bookhive.access.infrastructure.input.security;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class SessionCookieTokenResolver implements BearerTokenResolver {

	public static final String SESSION_COOKIE = "bookhive_session";

	@Override
	public String resolve(final HttpServletRequest request) {

		final Cookie[] cookies = request.getCookies();

		if (ObjectHelper.isNull(cookies)) {
			return null;
		}

		for (final Cookie cookie : cookies) {
			if (SESSION_COOKIE.equals(cookie.getName())
					&& !TextHelper.isDefaultValue(cookie.getValue())) {

				return cookie.getValue().trim();
			}
		}

		return null;
	}

}
