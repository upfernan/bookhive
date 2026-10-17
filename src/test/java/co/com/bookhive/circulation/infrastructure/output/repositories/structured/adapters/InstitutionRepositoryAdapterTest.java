package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import co.com.bookhive.circulation.testdata.FakeDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class InstitutionRepositoryAdapterTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	@Test
	@DisplayName("devuelve el esquema de la institución activa")
	void findsActiveSchema() {
		final InstitutionRepositoryAdapter adapter = new InstitutionRepositoryAdapter(
				FakeDataSource.withRow(Map.of("esquema", "institucion_uco")));

		assertEquals("institucion_uco", adapter.findActiveSchema(INSTITUTION));
	}

	@Test
	@DisplayName("rechaza una institución que no está activa")
	void rejectsInactiveInstitution() {
		final InstitutionRepositoryAdapter adapter = new InstitutionRepositoryAdapter(FakeDataSource.withoutRows());

		assertRejected(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, MessageKey.INSTITUTION_NOT_ACTIVE,
				() -> adapter.findActiveSchema(INSTITUTION));
	}

	@Test
	@DisplayName("no acepta un nombre de esquema que pueda dañar la consulta")
	void rejectsUnsafeSchemaName() {
		final InstitutionRepositoryAdapter adapter = new InstitutionRepositoryAdapter(
				FakeDataSource.withRow(Map.of("esquema", "uco; DROP SCHEMA compartido")));

		assertRejected(BookHiveException.Kind.UNEXPECTED, MessageKey.REQUEST_UNEXPECTED,
				() -> adapter.findActiveSchema(INSTITUTION));
	}

	@Test
	@DisplayName("si la base de datos falla responde con un error inesperado")
	void reportsDatabaseFailure() {
		final InstitutionRepositoryAdapter adapter = new InstitutionRepositoryAdapter(FakeDataSource.failing());

		assertRejected(BookHiveException.Kind.UNEXPECTED, MessageKey.REQUEST_UNEXPECTED,
				() -> adapter.findActiveSchema(INSTITUTION));
	}

	private static void assertRejected(final BookHiveException.Kind kind, final String messageKey,
			final Executable action) {
		final BookHiveException exception = assertThrows(BookHiveException.class, action);
		assertEquals(kind, exception.getKind());
		assertEquals(messageKey, exception.getUserMessageKey());
	}

}
