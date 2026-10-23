package co.com.bookhive.gateway.testdata;

import java.util.List;
import java.util.Map;

import co.com.bookhive.gateway.infrastructure.input.rules.EntryRulesProperties;

public final class GatewayRulesData {

	public static final EntryRulesProperties PROPERTIES = new EntryRulesProperties(
			List.of("GET /actuator/health/**", "POST /api/v1/sesiones", "DELETE /api/v1/sesiones", "/libre/**"),
			Map.of("abrir-sesion", "POST /api/v1/sesiones", "prestar", "post /api/v1/prestamos"),
			List.of("/api/v1/admin/**"),
			List.of("/api/v1/tareas/**", "/api/v1/avisos/**"));

	private GatewayRulesData() {
		super();
	}

}
