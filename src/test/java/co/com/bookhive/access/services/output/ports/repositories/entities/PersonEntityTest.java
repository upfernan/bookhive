package co.com.bookhive.access.services.output.ports.repositories.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.helper.UUIDHelper;

class PersonEntityTest {

	@Test
	@DisplayName("nace con identificadores por defecto")
	void startsWithDefaults() {
		final PersonEntity person = new PersonEntity();

		assertEquals(UUIDHelper.getDefaultValue(), person.getId());
		assertEquals(UUIDHelper.getDefaultValue(), person.getInstitution());
	}

	@Test
	@DisplayName("guarda la persona y su institución")
	void keepsValues() {
		final UUID id = UUIDHelper.generateNew();
		final UUID institution = UUIDHelper.generateNew();
		final PersonEntity person = new PersonEntity();

		person.setId(id);
		person.setInstitution(institution);

		assertEquals(id, person.getId());
		assertEquals(institution, person.getInstitution());
	}

	@Test
	@DisplayName("convierte los nulos en valores por defecto")
	void replacesNulls() {
		final PersonEntity person = new PersonEntity();

		person.setId(null);
		person.setInstitution(null);

		assertEquals(UUIDHelper.getDefaultValue(), person.getId());
		assertEquals(UUIDHelper.getDefaultValue(), person.getInstitution());
	}

}
