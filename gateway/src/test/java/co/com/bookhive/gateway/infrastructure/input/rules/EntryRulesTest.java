package co.com.bookhive.gateway.infrastructure.input.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import co.com.bookhive.gateway.testdata.GatewayRulesData;

class EntryRulesTest {

	private final EntryRules rules = new EntryRules(GatewayRulesData.PROPERTIES);

	@Test
	@DisplayName("las rutas públicas se reconocen por método y ruta")
	void publicRoutesMatchMethodAndPath() {
		assertTrue(rules.isPublic(MockServerHttpRequest.get("/actuator/health/liveness").build()));
		assertTrue(rules.isPublic(MockServerHttpRequest.post("/api/v1/sesiones").build()));
		assertTrue(rules.isPublic(MockServerHttpRequest.put("/libre/lo-que-sea").build()));
		assertFalse(rules.isPublic(MockServerHttpRequest.get("/api/v1/sesiones").build()));
		assertFalse(rules.isPublic(MockServerHttpRequest.post("/api/v1/prestamos").build()));
	}

	@Test
	@DisplayName("las acciones sensibles se nombran y lo demás no tiene acción")
	void sensitiveActionsAreNamed() {
		assertEquals("prestar", rules.actionOf(MockServerHttpRequest.post("/api/v1/prestamos").build()));
		assertEquals("abrir-sesion", rules.actionOf(MockServerHttpRequest.post("/api/v1/sesiones").build()));
		assertEquals("", rules.actionOf(MockServerHttpRequest.get("/api/v1/prestamos").build()));
	}

	@Test
	@DisplayName("las rutas administrativas se reconocen sin importar el método")
	void administrativePathsMatchAnyMethod() {
		assertTrue(rules.isAdministrative(MockServerHttpRequest.get("/api/v1/admin/roles").build()));
		assertTrue(rules.isAdministrative(MockServerHttpRequest.delete("/api/v1/admin/roles/1").build()));
		assertFalse(rules.isAdministrative(MockServerHttpRequest.post("/api/v1/prestamos").build()));
	}

	@Test
	@DisplayName("sin configuración no hay rutas públicas, acciones ni administración")
	void emptyPropertiesMeanNothingSpecial() {
		final EntryRules empty = new EntryRules(new EntryRulesProperties(null, null, null, null));

		assertFalse(empty.isPublic(MockServerHttpRequest.post("/api/v1/sesiones").build()));
		assertEquals("", empty.actionOf(MockServerHttpRequest.post("/api/v1/prestamos").build()));
		assertFalse(empty.isAdministrative(MockServerHttpRequest.get("/api/v1/admin").build()));
		assertFalse(empty.isInternal(MockServerHttpRequest.post("/api/v1/tareas/prestamos").build()));
	}

	@Test
	@DisplayName("las rutas internas de Google se reconocen sin importar el método")
	void internalPathsMatchAnyMethod() {
		assertTrue(rules.isInternal(MockServerHttpRequest.post("/api/v1/tareas/recordar-vencimientos").build()));
		assertTrue(rules.isInternal(MockServerHttpRequest.get("/api/v1/avisos/correo").build()));
		assertFalse(rules.isInternal(MockServerHttpRequest.post("/api/v1/prestamos").build()));
	}

}
