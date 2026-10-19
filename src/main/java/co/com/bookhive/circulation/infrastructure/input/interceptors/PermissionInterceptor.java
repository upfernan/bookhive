package co.com.bookhive.circulation.infrastructure.input.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import co.com.bookhive.circulation.infrastructure.input.security.RequiredPermission;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.PermissionRepository;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

	private final SessionContextPort sessionContext;
	private final PermissionRepository permissionRepository;

	public PermissionInterceptor(
			final SessionContextPort sessionContext,
			final PermissionRepository permissionRepository) {

		super();
		this.sessionContext = sessionContext;
		this.permissionRepository = permissionRepository;
	}

	@Override
	public boolean preHandle(
			final HttpServletRequest request,
			final HttpServletResponse response,
			final Object handler) {

		if (!(handler instanceof HandlerMethod method)) {
			return true;
		}

		final RequiredPermission required = method.getMethodAnnotation(RequiredPermission.class);

		if (ObjectHelper.isNull(required)) {
			throw BookHiveException.createNotAllowed(
					MessageKey.PERMISSION_DENIED,
					"La operación " + method.getShortLogMessage() + " no declara qué permiso exige,"
							+ " así que nadie puede usarla (ADR-027).");
		}

		if (!permissionRepository.isGranted(sessionContext.getLibrarian(), required.value())) {
			throw BookHiveException.createNotAllowed(
					MessageKey.PERMISSION_DENIED,
					"La persona en sesión no tiene el permiso " + required.value() + ".");
		}

		return true;
	}

}
