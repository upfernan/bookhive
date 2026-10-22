package co.com.bookhive.access.services.usecase.permission.authorizeoperation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.domain.permission.authorizeoperation.AuthorizeOperationDomain;
import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.testdata.FakeAccess;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class AuthorizeOperationUseCaseImplTest {

	private static final UUID PERSON = UUIDHelper.generateNew();
	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	private final List<String> audited = new ArrayList<>();

	@Test
	@DisplayName("deja pasar a quien tiene el permiso que exige la operación")
	void allowsGrantedPermission() {
		final FakeAccess.Permissions permissions =
				new FakeAccess.Permissions().with(FakeAccess.permission(PermissionCode.LEND_BOOK));

		useCase(permissions).execute(new AuthorizeOperationDomain(PermissionCode.LEND_BOOK, "LoanController#lend"));

		assertEquals(PERSON, permissions.searches().get(0).getPerson());
		assertEquals(PermissionCode.LEND_BOOK, permissions.searches().get(0).getCode());
		assertTrue(audited.isEmpty());
	}

	@Test
	@DisplayName("rechaza y deja constancia cuando la persona no tiene el permiso")
	void rejectsMissingPermission() {
		final AuthorizeOperationUseCaseImpl useCase = useCase(new FakeAccess.Permissions());
		final AuthorizeOperationDomain operation =
				new AuthorizeOperationDomain(PermissionCode.LEND_BOOK, "LoanController#lend");

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(operation));

		assertEquals(BookHiveException.Kind.NOT_ALLOWED, exception.getKind());
		assertEquals(MessageKey.PERMISSION_DENIED, exception.getUserMessageKey());
		assertEquals(List.of(AuditPort.PERMISSION_DENIED + " " + INSTITUTION + " " + PERSON + " "
				+ PermissionCode.LEND_BOOK), audited);
	}

	@Test
	@DisplayName("una operación que no declara su permiso no la puede usar nadie")
	void rejectsUndeclaredOperation() {
		final FakeAccess.Permissions permissions =
				new FakeAccess.Permissions().with(FakeAccess.permission(PermissionCode.LEND_BOOK));
		final AuthorizeOperationUseCaseImpl useCase = useCase(permissions);

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(BookHiveException.Kind.NOT_ALLOWED, exception.getKind());
		assertTrue(permissions.searches().isEmpty());
	}

	private AuthorizeOperationUseCaseImpl useCase(final FakeAccess.Permissions permissions) {
		return new AuthorizeOperationUseCaseImpl(
				() -> new ResolveSessionDomain(PERSON, INSTITUTION),
				permissions,
				(action, institution, person, detail) -> audited.add(action + " " + institution + " " + person + " " + detail));
	}

}
