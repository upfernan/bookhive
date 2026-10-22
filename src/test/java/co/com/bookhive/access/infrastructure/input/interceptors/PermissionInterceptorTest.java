package co.com.bookhive.access.infrastructure.input.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import co.com.bookhive.access.services.output.ports.SessionContextPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class PermissionInterceptorTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();
	private static final UUID LIBRARIAN = UUIDHelper.generateNew();

	private List<String> checks;
	private List<String> audited;

	@BeforeEach
	void prepare() {
		checks = new ArrayList<>();
		audited = new ArrayList<>();
	}

	@Test
	@DisplayName("deja pasar a quien tiene el permiso que exige la operación")
	void allowsGrantedPermission() throws NoSuchMethodException {
		final PermissionInterceptor interceptor = interceptor(true);

		assertTrue(interceptor.preHandle(null, null, operation("lend")));
		assertEquals(List.of(LIBRARIAN + " " + PermissionCode.LEND_BOOK), checks);
		assertTrue(audited.isEmpty());
	}

	@Test
	@DisplayName("rechaza a quien no tiene el permiso")
	void rejectsMissingPermission() throws NoSuchMethodException {
		final PermissionInterceptor interceptor = interceptor(false);
		final HandlerMethod lend = operation("lend");

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> interceptor.preHandle(null, null, lend));

		assertEquals(BookHiveException.Kind.NOT_ALLOWED, exception.getKind());
		assertEquals(MessageKey.PERMISSION_DENIED, exception.getUserMessageKey());
		assertEquals(List.of(AuditPort.PERMISSION_DENIED + " " + INSTITUTION + " " + LIBRARIAN + " "
				+ PermissionCode.LEND_BOOK), audited);
	}

	@Test
	@DisplayName("una operación que no declara su permiso no la puede usar nadie")
	void rejectsUndeclaredOperation() throws NoSuchMethodException {
		final PermissionInterceptor interceptor = interceptor(true);
		final HandlerMethod undeclared = operation("undeclared");

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> interceptor.preHandle(null, null, undeclared));

		assertEquals(BookHiveException.Kind.NOT_ALLOWED, exception.getKind());
		assertTrue(checks.isEmpty());
	}

	@Test
	@DisplayName("lo que no es una operación de un controlador no se revisa")
	void ignoresOtherHandlers() {
		assertTrue(interceptor(false).preHandle(null, null, new Object()));
		assertTrue(checks.isEmpty());
	}

	private PermissionInterceptor interceptor(final boolean granted) {
		return new PermissionInterceptor(new FixedSession(), (person, permission) -> {
			checks.add(person + " " + permission);
			return granted;
		}, (action, institution, person, detail) -> audited.add(action + " " + institution + " " + person + " " + detail));
	}

	private static HandlerMethod operation(final String name) throws NoSuchMethodException {
		return new HandlerMethod(new SampleController(), SampleController.class.getDeclaredMethod(name));
	}

	private static final class FixedSession implements SessionContextPort {

		@Override
		public UUID getInstitution() {
			return INSTITUTION;
		}

		@Override
		public UUID getLibrarian() {
			return LIBRARIAN;
		}

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
