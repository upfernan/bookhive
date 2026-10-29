package co.com.bookhive.crosscutting.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TextHelperTest {

	@Test
	@DisplayName("convierte el nulo en texto vacío")
	void replacesNullWithEmpty() {
		assertEquals("", TextHelper.getDefaultValue(null));
	}

	@Test
	@DisplayName("quita los espacios de los extremos")
	void trimsText() {
		assertEquals("BookHive", TextHelper.getDefaultValue("  BookHive  "));
	}

	@Test
	@DisplayName("un texto con solo espacios cuenta como vacío")
	void blankIsDefault() {
		assertTrue(TextHelper.isDefaultValue("   "));
		assertFalse(TextHelper.isDefaultValue("BookHive"));
	}

}
