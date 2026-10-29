package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.services.output.ports.repositories.criteria.InstitutionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.access.testdata.FakeDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class InstitutionRepositoryAdapterTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	@Test
	@DisplayName("devuelve las instituciones que cumplen el criterio, con su esquema")
	void findsInstitutionsByCriteria() {
		final InstitutionRepositoryAdapter adapter = new InstitutionRepositoryAdapter(
				FakeDataSource.withRow(Map.of("id", INSTITUTION, "esquema", "institucion_uco")));

		final List<InstitutionEntity> institutions =
				adapter.findByCriteria(InstitutionCriteria.activeById(INSTITUTION));

		assertEquals(INSTITUTION, institutions.get(0).getId());
		assertEquals("institucion_uco", institutions.get(0).getSchema());
	}

	@Test
	@DisplayName("si ninguna institución cumple el criterio devuelve una lista vacía")
	void returnsEmptyWhenNoneMatches() {
		final InstitutionRepositoryAdapter adapter = new InstitutionRepositoryAdapter(FakeDataSource.withoutRows());

		assertTrue(adapter.findByCriteria(InstitutionCriteria.activeById(INSTITUTION)).isEmpty());
	}

	@Test
	@DisplayName("sin criterio no consulta nada")
	void withoutCriteriaReturnsNothing() {
		final InstitutionRepositoryAdapter adapter = new InstitutionRepositoryAdapter(FakeDataSource.failing());

		assertTrue(adapter.findByCriteria(null).isEmpty());
	}

	@Test
	@DisplayName("no acepta un nombre de esquema que pueda dañar la consulta")
	void rejectsUnsafeSchemaName() {
		final InstitutionRepositoryAdapter adapter = new InstitutionRepositoryAdapter(
				FakeDataSource.withRow(Map.of("id", INSTITUTION, "esquema", "uco; DROP SCHEMA compartido")));
		final InstitutionCriteria criteria = InstitutionCriteria.activeById(INSTITUTION);

		assertThrows(BookHiveException.class, () -> adapter.findByCriteria(criteria));
	}

	@Test
	@DisplayName("si la base de datos falla responde con un error inesperado")
	void reportsDatabaseFailure() {
		final InstitutionRepositoryAdapter adapter = new InstitutionRepositoryAdapter(FakeDataSource.failing());
		final InstitutionCriteria criteria = InstitutionCriteria.activeById(INSTITUTION);

		final BookHiveException exception =
				assertThrows(BookHiveException.class, () -> adapter.findByCriteria(criteria));

		assertEquals(MessageKey.REQUEST_UNEXPECTED, exception.getUserMessageKey());
	}

}
