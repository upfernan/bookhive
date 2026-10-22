package co.com.bookhive.access.services.usecase.institution.listactiveschemas.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.domain.institution.listactiveschemas.ListActiveSchemasDomain;
import co.com.bookhive.access.services.input.adapters.ListActiveSchemasInputAdapter;
import co.com.bookhive.access.services.input.dto.response.ListActiveSchemasResponseDTO;
import co.com.bookhive.access.services.output.ports.repositories.criteria.InstitutionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.access.testdata.FakeAccess;

class ListActiveSchemasUseCaseImplTest {

	@Test
	@DisplayName("lista los esquemas de las instituciones activas")
	void listsActiveSchemas() {
		final FakeAccess.Institutions institutions = new FakeAccess.Institutions()
				.with(FakeAccess.institution(UUID.randomUUID(), "uco"))
				.with(FakeAccess.institution(UUID.randomUUID(), "unal"));

		final ListActiveSchemasDomain result = new ListActiveSchemasUseCaseImpl(institutions).execute();

		assertEquals(List.of("uco", "unal"), result.schemas());
		final InstitutionCriteria criteria = institutions.searches().get(0);
		assertEquals(InstitutionEntity.STATUS_ACTIVE, criteria.getStatus());
		assertTrue(criteria.filtersByStatus());
		assertFalse(criteria.filtersById());
	}

	@Test
	@DisplayName("el puerto de entrada devuelve los mismos esquemas")
	void inputAdapterReturnsSchemas() {
		final ListActiveSchemasResponseDTO response =
				new ListActiveSchemasInputAdapter(() -> new ListActiveSchemasDomain(List.of("uco"))).execute();

		assertEquals(List.of("uco"), response.schemas());
		assertEquals(List.of(), new ListActiveSchemasResponseDTO(null).schemas());
		assertEquals(List.of(), new ListActiveSchemasDomain(null).schemas());
	}

}
