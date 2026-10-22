package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserCriteriaTest {

	@Test
	@DisplayName("guarda el correo sin espacios y en minúscula para comparar sin distinguir mayúsculas")
	void normalizesEmail() {
		final UserCriteria criteria = UserCriteria.byEmail("  ANA.Gomez@Uco.edu.co ");

		assertEquals("ana.gomez@uco.edu.co", criteria.getEmail());
		assertTrue(criteria.filtersByEmail());
	}

	@Test
	@DisplayName("un correo vacío o nulo no filtra")
	void emptyEmailDoesNotFilter() {
		assertFalse(UserCriteria.byEmail(null).filtersByEmail());
		assertFalse(UserCriteria.byEmail("   ").filtersByEmail());
	}

}
