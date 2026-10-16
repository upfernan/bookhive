package co.com.bookhive.initializer.tracing;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class InstitutionLoggingFilter extends OncePerRequestFilter {

	static final String MDC_KEY = "institucion";
	static final String UNRESOLVED = "sin-institucion";

	@Override
	protected void doFilterInternal(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final FilterChain chain) throws ServletException, IOException {

		try {
			MDC.put(MDC_KEY, resolveInstitution());
			chain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}

	private String resolveInstitution() {
		return UNRESOLVED;
	}

}
