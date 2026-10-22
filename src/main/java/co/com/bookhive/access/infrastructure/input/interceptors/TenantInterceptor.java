package co.com.bookhive.access.infrastructure.input.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import co.com.bookhive.access.services.input.ports.ResolveSchemaInputPort;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantInterceptor implements HandlerInterceptor {

	private final ResolveSchemaInputPort resolveSchema;

	public TenantInterceptor(final ResolveSchemaInputPort resolveSchema) {
		super();
		this.resolveSchema = resolveSchema;
	}

	@Override
	public boolean preHandle(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final Object handler) {

		TenantContext.set(resolveSchema.execute().schema());

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
