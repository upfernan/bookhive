package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.access.testdata.FakeDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class PersonRepositoryAdapterTest {

	private static final String IDENTITY = "persona-del-pase";

	@Test
	@DisplayName("devuelve la persona activa con su institución")
	void findsActivePerson() {
		final UUID id = UUIDHelper.generateNew();
		final UUID institution = UUIDHelper.generateNew();
		final PersonRepositoryAdapter adapter = new PersonRepositoryAdapter(
				FakeDataSource.withRow(Map.of("id", id, "institucion_id", institution)));

		final PersonEntity person = adapter.findActive(IDENTITY);

		assertEquals(id, person.getId());
		assertEquals(institution, person.getInstitution());
	}

	@Test
	@DisplayName("rechaza un pase sin identificador de persona")
	void rejectsBlankIdentity() {
		final PersonRepositoryAdapter adapter = new PersonRepositoryAdapter(FakeDataSource.withoutRows());

		assertRejected(BookHiveException.Kind.UNEXPECTED, MessageKey.SESSION_NOT_RESOLVED, () -> adapter.findActive("  "));
	}

	@Test
	@DisplayName("rechaza a una persona que no está activa en BookHive")
	void rejectsUnknownPerson() {
		final PersonRepositoryAdapter adapter = new PersonRepositoryAdapter(FakeDataSource.withoutRows());

		assertRejected(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, MessageKey.SESSION_NOT_RESOLVED,
				() -> adapter.findActive(IDENTITY));
	}

	@Test
	@DisplayName("si la base de datos falla responde con un error inesperado")
	void reportsDatabaseFailure() {
		final PersonRepositoryAdapter adapter = new PersonRepositoryAdapter(FakeDataSource.failing());

		assertRejected(BookHiveException.Kind.UNEXPECTED, MessageKey.REQUEST_UNEXPECTED,
				() -> adapter.findActive(IDENTITY));
	}

	private static void assertRejected(final BookHiveException.Kind kind, final String messageKey,
			final Executable action) {
		final BookHiveException exception = assertThrows(BookHiveException.class, action);
		assertEquals(kind, exception.getKind());
		assertEquals(messageKey, exception.getUserMessageKey());
	}

}
