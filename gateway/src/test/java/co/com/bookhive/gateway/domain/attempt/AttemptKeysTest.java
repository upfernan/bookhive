package co.com.bookhive.gateway.domain.attempt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttemptKeysTest {

	private static final AttemptRequest LEND = new AttemptRequest("prestar", "uid-1", "191.91.85.226", false);

	@Test
	@DisplayName("las llaves separan contador y bloqueo, cuenta y origen, y llevan la acción")
	void keysSeparateScopes() {
		assertEquals("bookhive:intentos:cuenta:uid-1:prestar", AttemptKeys.accountCounter(LEND));
		assertEquals("bookhive:bloqueo:cuenta:uid-1:prestar", AttemptKeys.accountBlock(LEND));
		assertEquals("bookhive:intentos:origen:191.91.85.226:prestar", AttemptKeys.originCounter(LEND));
		assertEquals("bookhive:bloqueo:origen:191.91.85.226:prestar", AttemptKeys.originBlock(LEND));
	}

	@Test
	@DisplayName("una solicitud sin acción sensible usa la acción general")
	void requestWithoutActionUsesGeneral() {
		final AttemptRequest admin = new AttemptRequest("", "", "10.0.0.1", true);

		assertEquals("bookhive:bloqueo:origen:10.0.0.1:general", AttemptKeys.originBlock(admin));
	}

	@Test
	@DisplayName("los caracteres raros no pueden partir ni alargar la llave")
	void unsafeCharactersAreReplaced() {
		final AttemptRequest odd = new AttemptRequest("prestar", "a b*c\n" + "x".repeat(300), "::1", false);
		final String key = AttemptKeys.accountBlock(odd);

		assertFalse(key.contains(" "));
		assertFalse(key.contains("*"));
		assertFalse(key.contains("\n"));
		assertTrue(key.startsWith("bookhive:bloqueo:cuenta:a_b_c_"));
		assertEquals(128, AttemptKeys.safe("y".repeat(500)).length());
	}

	@Test
	@DisplayName("los campos vacíos se normalizan y el origen desconocido tiene nombre")
	void emptyFieldsAreNormalized() {
		final AttemptRequest empty = new AttemptRequest(null, null, null, false);

		assertFalse(empty.isSensitive());
		assertFalse(empty.hasAccount());
		assertFalse(empty.needsGuard());
		assertEquals("desconocido", empty.origin());
	}

}
