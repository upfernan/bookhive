package co.com.bookhive.access.services.usecase.session.resolveschema.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.domain.session.resolveschema.ResolveSchemaDomain;
import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.access.testdata.FakeAccess;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

class ResolveSchemaUseCaseImplTest {

	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	@Test
	@DisplayName("elige el esquema de la institución activa de la persona en sesión")
	void resolvesSchemaOfActiveInstitution() {
		final FakeAccess.Institutions institutions =
				new FakeAccess.Institutions().with(FakeAccess.institution(INSTITUTION, "uco"));

		final ResolveSchemaDomain schema = new ResolveSchemaUseCaseImpl(
				() -> new ResolveSessionDomain(UUIDHelper.generateNew(), INSTITUTION), institutions).execute();

		assertEquals("uco", schema.schema());
		assertEquals(INSTITUTION, schema.institution());
		assertEquals(INSTITUTION, institutions.searches().get(0).getId());
		assertEquals(InstitutionEntity.STATUS_ACTIVE, institutions.searches().get(0).getStatus());
	}

	@Test
	@DisplayName("si la institución no está activa no deja seguir")
	void rejectsInactiveInstitution() {
		final ResolveSchemaUseCaseImpl useCase = new ResolveSchemaUseCaseImpl(
				() -> new ResolveSessionDomain(UUIDHelper.generateNew(), INSTITUTION), new FakeAccess.Institutions());

		final BookHiveException exception = assertThrows(BookHiveException.class, useCase::execute);

		assertEquals(MessageKey.INSTITUTION_NOT_ACTIVE, exception.getUserMessageKey());
	}

}
