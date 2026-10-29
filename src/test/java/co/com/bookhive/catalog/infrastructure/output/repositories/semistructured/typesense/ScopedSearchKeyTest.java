package co.com.bookhive.catalog.infrastructure.output.repositories.semistructured.typesense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class ScopedSearchKeyTest {

	private static final String OFFICIAL_SEARCH_KEY = "RN23GFr1s6jQ9kgSNg2O7fYcAUXU7127";
	private static final String OFFICIAL_SCOPED_KEY =
			"SC9sT0hncHFwTHNFc3U3d3psRDZBUGNXQUViQUdDNmRHSmJFQnNnczJ4VT1STjIzeyJmaWx0ZXJfYnkiOiJjb21wYW55X2lkOjEyNCJ9";

	@Test
	@DisplayName("genera exactamente la clave del ejemplo oficial de Typesense, verificado contra un servidor real")
	void matchesTypesenseReferenceKey() {
		assertEquals(OFFICIAL_SCOPED_KEY,
				ScopedSearchKey.generate(OFFICIAL_SEARCH_KEY, "{\"filter_by\":\"company_id:124\"}"));
	}

	@Test
	@DisplayName("la clave de una institución lleva adentro el filtro que la limita a su catálogo")
	void institutionKeyEmbedsItsFilter() {
		final String decoded = new String(
				Base64.getDecoder().decode(ScopedSearchKey.forInstitution(OFFICIAL_SEARCH_KEY, "uco")),
				StandardCharsets.UTF_8);

		assertTrue(decoded.endsWith("RN23{\"filter_by\":\"institution:=uco\"}"));
		assertNotEquals(ScopedSearchKey.forInstitution(OFFICIAL_SEARCH_KEY, "uco"),
				ScopedSearchKey.forInstitution(OFFICIAL_SEARCH_KEY, "unal"));
	}

	@Test
	@DisplayName("la clave de búsqueda que se deriva de la clave de administración siempre es la misma")
	void derivedKeyIsStable() {
		final String derived = ScopedSearchKey.derive("clave-admin", "bookhive-catalog-search");

		assertEquals(64, derived.length());
		assertEquals(derived, ScopedSearchKey.derive("clave-admin", "bookhive-catalog-search"));
		assertNotEquals(derived, ScopedSearchKey.derive("otra-clave", "bookhive-catalog-search"));
	}

	@Test
	@DisplayName("sin clave de administración no puede firmar")
	void failsWithoutKey() {
		final BookHiveException exception =
				assertThrows(BookHiveException.class, () -> ScopedSearchKey.derive("", "bookhive-catalog-search"));

		assertEquals(MessageKey.SEARCH_UNAVAILABLE, exception.getUserMessageKey());
	}

}
