package co.com.bookhive.gateway.crosscutting.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TextHelperTest {

	@Test
	@DisplayName("nunca devuelve nulos y recorta los espacios")
	void neverReturnsNull() {
		assertEquals("", TextHelper.getDefaultValue());
		assertEquals("", TextHelper.getDefaultValue(null));
		assertEquals("hola", TextHelper.getDefaultValue("  hola "));
		assertEquals("respaldo", TextHelper.getDefaultValue(" ", " respaldo "));
		assertEquals("valor", TextHelper.getDefaultValue("valor", "respaldo"));
		assertTrue(TextHelper.isDefaultValue("   "));
		assertFalse(TextHelper.isDefaultValue("x"));
	}

}
