package co.com.bookhive.gateway.infrastructure.input.rules;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bookhive.gateway.rules")
public record EntryRulesProperties(
		List<String> publicRoutes,
		Map<String, String> sensitiveActions,
		List<String> administrativePaths) {

	public EntryRulesProperties {
		publicRoutes = publicRoutes == null ? List.of() : List.copyOf(publicRoutes);
		sensitiveActions = sensitiveActions == null ? Map.of() : Map.copyOf(sensitiveActions);
		administrativePaths = administrativePaths == null ? List.of() : List.copyOf(administrativePaths);
	}

}
