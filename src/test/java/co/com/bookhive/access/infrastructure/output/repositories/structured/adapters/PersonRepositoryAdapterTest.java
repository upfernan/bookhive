package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.services.output.ports.repositories.criteria.PersonCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.access.testdata.FakeDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class PersonRepositoryAdapterTest {

	@Test
	@DisplayName("devuelve las personas que cumplen el criterio, con su institución")
	void findsPeopleByCriteria() {
		final UUID id = UUIDHelper.generateNew();
		final UUID institution = UUIDHelper.generateNew();
		final PersonRepositoryAdapter adapter = new PersonRepositoryAdapter(
				FakeDataSource.withRow(Map.of("id", id, "institucion_id", institution)));

		final List<PersonEntity> people = adapter.findByCriteria(PersonCriteria.activeByIdentity("uid"));

		assertEquals(1, people.size());
		assertEquals(id, people.get(0).getId());
		assertEquals(institution, people.get(0).getInstitution());
	}

	@Test
	@DisplayName("si nadie cumple el criterio devuelve una lista vacía")
	void returnsEmptyWhenNobodyMatches() {
		final PersonRepositoryAdapter adapter = new PersonRepositoryAdapter(FakeDataSource.withoutRows());

		assertTrue(adapter.findByCriteria(PersonCriteria.activeByIdentity("uid")).isEmpty());
	}

	@Test
	@DisplayName("sin criterio no consulta nada")
	void withoutCriteriaReturnsNothing() {
		final PersonRepositoryAdapter adapter = new PersonRepositoryAdapter(FakeDataSource.failing());

		assertTrue(adapter.findByCriteria(null).isEmpty());
	}

	@Test
	@DisplayName("si la base de datos falla responde con un error inesperado")
	void reportsDatabaseFailure() {
		final PersonRepositoryAdapter adapter = new PersonRepositoryAdapter(FakeDataSource.failing());
		final PersonCriteria criteria = PersonCriteria.activeByIdentity("uid");

		final BookHiveException exception =
				assertThrows(BookHiveException.class, () -> adapter.findByCriteria(criteria));

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
	}

}
