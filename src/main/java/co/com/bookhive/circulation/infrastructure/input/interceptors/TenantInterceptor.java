package co.com.bookhive.circulation.infrastructure.input.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.tenancy.TenantContext;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantInterceptor implements HandlerInterceptor {

	private final SessionContextPort sessionContext;
	private final InstitutionRepository institutionRepository;

	public TenantInterceptor(
			final SessionContextPort sessionContext,
			final InstitutionRepository institutionRepository) {

		super();
		this.sessionContext = sessionContext;
		this.institutionRepository = institutionRepository;
	}

	@Override
	public boolean preHandle(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final Object handler) {

		TenantContext.set(
				institutionRepository.findActiveSchema(sessionContext.getInstitution()));

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
