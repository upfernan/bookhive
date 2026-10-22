package co.com.bookhive.access.infrastructure.input.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import co.com.bookhive.access.services.input.dto.request.AuthorizeOperationRequestDTO;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

class PermissionInterceptorTest {

	private final List<AuthorizeOperationRequestDTO> asked = new ArrayList<>();

	private final PermissionInterceptor interceptor = new PermissionInterceptor(asked::add);

	@Test
	@DisplayName("pregunta por el permiso que declara la operación")
	void asksForDeclaredPermission() throws NoSuchMethodException {
		assertTrue(interceptor.preHandle(null, null, operation("lend")));

		assertEquals(PermissionCode.LEND_BOOK, asked.get(0).permission());
		assertTrue(asked.get(0).operation().contains("lend"));
	}

	@Test
	@DisplayName("si la operación no declara permiso pregunta sin permiso para que se rechace")
	void asksWithoutPermissionForUndeclaredOperation() throws NoSuchMethodException {
		interceptor.preHandle(null, null, operation("undeclared"));

		assertEquals("", asked.get(0).permission());
	}

	@Test
	@DisplayName("lo que no es una operación de un controlador no se revisa")
	void ignoresOtherHandlers() {
		assertTrue(interceptor.preHandle(null, null, new Object()));
		assertTrue(asked.isEmpty());
	}

	private static HandlerMethod operation(final String name) throws NoSuchMethodException {
		return new HandlerMethod(new SampleController(), SampleController.class.getDeclaredMethod(name));
	}

	static final class SampleController {

		@RequiredPermission(PermissionCode.LEND_BOOK)
		String lend() {
			return "prestado";
		}

		String undeclared() {
			return "sin permiso";
		}

	}

}
