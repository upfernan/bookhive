package co.com.bookhive.initializer.migrations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MigrationPlaceholdersTest {

	@Test
	@DisplayName("sin usuario configurado usa el usuario de la aplicación por defecto")
	void usesDefaultApplicationUser() {
		assertEquals(Map.of("appUser", "bookhive_app"), MigrationPlaceholders.forApplicationUser(null));
		assertEquals(Map.of("appUser", "bookhive_app"), MigrationPlaceholders.forApplicationUser("  "));
	}

	@Test
	@DisplayName("usa el usuario configurado")
	void usesConfiguredApplicationUser() {
		assertEquals(Map.of("appUser", "bookhive_demo"), MigrationPlaceholders.forApplicationUser(" bookhive_demo "));
	}

	@Test
	@DisplayName("no acepta un nombre que pueda dañar las sentencias de permisos")
	void rejectsUnsafeName() {
		assertThrows(IllegalArgumentException.class,
				() -> MigrationPlaceholders.forApplicationUser("app; DROP SCHEMA compartido"));
	}

}
