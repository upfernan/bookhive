package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.services.output.ports.repositories.criteria.PermissionCriteria;
import co.com.bookhive.access.testdata.FakeDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

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
	@DisplayName("devuelve el permiso cuando alguno de los roles de la persona lo tiene")
	void findsGrantedPermission() {
		final PermissionRepositoryAdapter adapter = new PermissionRepositoryAdapter(
				FakeDataSource.withRow(Map.of("permission_code", PermissionCode.LEND_BOOK)));

		assertEquals(PermissionCode.LEND_BOOK,
				adapter.findByCriteria(PermissionCriteria.grantedTo(PERSON, PermissionCode.LEND_BOOK)).get(0).getCode());
	}

	@Test
	@DisplayName("no devuelve nada cuando ningún rol de la persona lo tiene")
	void findsNothingWhenNoRoleHasIt() {
		final PermissionRepositoryAdapter adapter = new PermissionRepositoryAdapter(FakeDataSource.withoutRows());

		assertTrue(adapter.findByCriteria(PermissionCriteria.grantedTo(PERSON, PermissionCode.LEND_BOOK)).isEmpty());
	}

	@Test
	@DisplayName("sin criterio no consulta nada")
	void withoutCriteriaReturnsNothing() {
		final PermissionRepositoryAdapter adapter = new PermissionRepositoryAdapter(FakeDataSource.failing());

		assertTrue(adapter.findByCriteria(null).isEmpty());
	}

	@Test
	@DisplayName("si la base de datos falla responde con un error inesperado")
	void reportsDatabaseFailure() {
		final PermissionRepositoryAdapter adapter = new PermissionRepositoryAdapter(FakeDataSource.failing());
		final PermissionCriteria criteria = PermissionCriteria.grantedTo(PERSON, PermissionCode.LEND_BOOK);

		final BookHiveException exception =
				assertThrows(BookHiveException.class, () -> adapter.findByCriteria(criteria));

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("no consulta con un nombre de esquema que pueda dañar la consulta")
	void rejectsUnsafeSchema() {
		TenantContext.set("uco; DROP SCHEMA compartido");
		final PermissionRepositoryAdapter adapter = new PermissionRepositoryAdapter(
				FakeDataSource.withRow(Map.of("permission_code", PermissionCode.LEND_BOOK)));
		final PermissionCriteria criteria = PermissionCriteria.grantedTo(PERSON, PermissionCode.LEND_BOOK);

		assertThrows(BookHiveException.class, () -> adapter.findByCriteria(criteria));
	}

}
