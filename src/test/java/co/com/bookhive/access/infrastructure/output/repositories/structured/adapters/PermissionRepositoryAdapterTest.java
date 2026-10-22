package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantContext;
import co.com.bookhive.access.testdata.FakeDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.security.PermissionCode;

class PermissionRepositoryAdapterTest {

	private static final UUID PERSON = UUIDHelper.generateNew();

	@BeforeEach
	void selectInstitution() {
		TenantContext.set("uco");
	}

	@AfterEach
	void clearInstitution() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("concede el permiso cuando alguno de los roles de la persona lo tiene")
	void grantsWhenARoleHasIt() {
		final PermissionRepositoryAdapter adapter =
				new PermissionRepositoryAdapter(FakeDataSource.withRow(Map.of("granted", 1)));

		assertTrue(adapter.isGranted(PERSON, PermissionCode.LEND_BOOK));
	}

	@Test
	@DisplayName("niega el permiso cuando ningún rol de la persona lo tiene")
	void deniesWhenNoRoleHasIt() {
		final PermissionRepositoryAdapter adapter = new PermissionRepositoryAdapter(FakeDataSource.withoutRows());

		assertFalse(adapter.isGranted(PERSON, PermissionCode.LEND_BOOK));
	}

	@Test
	@DisplayName("sin persona o sin permiso no consulta y niega")
	void deniesWithoutData() {
		final PermissionRepositoryAdapter adapter = new PermissionRepositoryAdapter(FakeDataSource.failing());

		assertFalse(adapter.isGranted(null, PermissionCode.LEND_BOOK));
		assertFalse(adapter.isGranted(PERSON, "  "));
	}

	@Test
	@DisplayName("si la base de datos falla responde con un error inesperado")
	void reportsDatabaseFailure() {
		final PermissionRepositoryAdapter adapter = new PermissionRepositoryAdapter(FakeDataSource.failing());

		final BookHiveException exception = assertThrows(BookHiveException.class,
				() -> adapter.isGranted(PERSON, PermissionCode.LEND_BOOK));

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("no consulta con un nombre de esquema que pueda dañar la consulta")
	void rejectsUnsafeSchema() {
		TenantContext.set("uco; DROP SCHEMA compartido");
		final PermissionRepositoryAdapter adapter =
				new PermissionRepositoryAdapter(FakeDataSource.withRow(Map.of("granted", 1)));

		assertThrows(BookHiveException.class, () -> adapter.isGranted(PERSON, PermissionCode.LEND_BOOK));
	}

}
