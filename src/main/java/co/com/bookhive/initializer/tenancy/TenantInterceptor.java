package co.com.bookhive.initializer.tenancy;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import co.com.bookhive.circulation.services.output.ports.SessionContextPort;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantInterceptor implements HandlerInterceptor {

	private final SessionContextPort sessionContext;
	private final InstitutionDirectory institutionDirectory;

	public TenantInterceptor(
			final SessionContextPort sessionContext,
			final InstitutionDirectory institutionDirectory) {

		super();
		this.sessionContext = sessionContext;
		this.institutionDirectory = institutionDirectory;
	}

	@Override
	public boolean preHandle(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final Object handler) {

		TenantContext.set(
				institutionDirectory.findActiveSchema(sessionContext.getInstitution()));

		return true;
	}

	@Override
	public void afterCompletion(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final Object handler,
			final Exception exception) {

		TenantContext.clear();
	}

}
