package co.com.bookhive.access.infrastructure.input.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import co.com.bookhive.access.services.input.dto.request.AuthorizeOperationRequestDTO;
import co.com.bookhive.access.services.input.ports.AuthorizeOperationInputPort;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.security.RequiredPermission;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

	private final AuthorizeOperationInputPort authorizeOperation;

	public PermissionInterceptor(final AuthorizeOperationInputPort authorizeOperation) {
		super();
		this.authorizeOperation = authorizeOperation;
	}

	@Override
	public boolean preHandle(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final Object handler) {

		if (handler instanceof HandlerMethod method) {
			authorize(method);
		}

		return true;
	}

	private void authorize(final HandlerMethod method) {
		final RequiredPermission required = method.getMethodAnnotation(RequiredPermission.class);
		final String permission = ObjectHelper.isNull(required) ? "" : required.value();

		authorizeOperation.execute(new AuthorizeOperationRequestDTO(permission, method.getShortLogMessage()));
	}

}
