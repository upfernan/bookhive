package co.com.bookhive.access.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.services.output.ports.repositories.criteria.DataPolicyAcceptanceCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyAcceptanceEntity;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyEntity;
import co.com.bookhive.access.testdata.FakeDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

class DataPolicyRepositoryAdaptersTest {

	private static final UUID PERSON = UUIDHelper.generateNew();
	private static final OffsetDateTime MOMENT = OffsetDateTime.of(2026, 10, 26, 16, 0, 0, 0, ZoneOffset.ofHours(-5));

	@BeforeEach
	void selectInstitution() {
		TenantContext.set("uco");
	}

	@AfterEach
	void clearInstitution() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("trae la versión vigente de la política con su texto y fecha")
	void findsCurrentPolicy() {
		final DataPolicyRepositoryAdapter adapter = new DataPolicyRepositoryAdapter(FakeDataSource.withRow(
				Map.of("version", 3, "content", "Texto vigente", "published_at", MOMENT)));

		assertEquals(new DataPolicyEntity(3, "Texto vigente", Instant.parse("2026-10-26T21:00:00Z")),
				adapter.findCurrent().orElseThrow());
		assertTrue(new DataPolicyRepositoryAdapter(FakeDataSource.withoutRows()).findCurrent().isEmpty());
	}

	@Test
	@DisplayName("trae la aceptación de la persona, de la versión vigente o de una versión dada")
	void findsAcceptance() {
		final DataPolicyAcceptanceEntity expected = new DataPolicyAcceptanceEntity(PERSON, 3, Instant.parse("2026-10-26T21:00:00Z"));
		final DataPolicyAcceptanceRepositoryAdapter adapter = acceptanceRow();

		assertEquals(expected, acceptanceRow().findByCriteria(DataPolicyAcceptanceCriteria.currentPolicyOf(PERSON)).get(0));
		assertEquals(expected, acceptanceRow().findByCriteria(DataPolicyAcceptanceCriteria.of(PERSON, 3)).get(0));
		assertTrue(adapter.findByCriteria(null).isEmpty());
		assertDoesNotThrow(() -> adapter.create(expected));
	}

	private static DataPolicyAcceptanceRepositoryAdapter acceptanceRow() {
		return new DataPolicyAcceptanceRepositoryAdapter(FakeDataSource.withRow(
				Map.of("person_id", PERSON, "policy_version", 3, "accepted_at", MOMENT)));
	}

	@Test
	@DisplayName("si la base de datos falla responde con un error inesperado")
	void reportsDatabaseFailure() {
		final DataPolicyAcceptanceRepositoryAdapter acceptances = new DataPolicyAcceptanceRepositoryAdapter(FakeDataSource.failing());
		final DataPolicyRepositoryAdapter policies = new DataPolicyRepositoryAdapter(FakeDataSource.failing());
		final DataPolicyAcceptanceCriteria criteria = DataPolicyAcceptanceCriteria.currentPolicyOf(PERSON);

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> acceptances.findByCriteria(criteria)).getKind());
		assertEquals(BookHiveException.Kind.UNEXPECTED, assertThrows(BookHiveException.class, policies::findCurrent).getKind());
	}

}
