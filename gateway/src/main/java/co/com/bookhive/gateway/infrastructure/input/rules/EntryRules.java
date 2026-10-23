package co.com.bookhive.gateway.infrastructure.input.rules;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import co.com.bookhive.gateway.crosscutting.helper.TextHelper;

@Component
public class EntryRules {

	private static final String ANY_METHOD = "*";
	private static final String SPACE = " ";

	private final List<Route> publicRoutes;
	private final List<NamedRoute> sensitiveActions;
	private final List<PathPattern> administrativePaths;
	private final List<PathPattern> internalPaths;

	public EntryRules(final EntryRulesProperties properties) {
		super();
		this.publicRoutes = properties.publicRoutes().stream().map(EntryRules::route).toList();
		this.sensitiveActions = properties.sensitiveActions().entrySet().stream()
				.map(EntryRules::namedRoute)
				.toList();
		this.administrativePaths = patternsOf(properties.administrativePaths());
		this.internalPaths = patternsOf(properties.internalPaths());
	}

	public boolean isPublic(final ServerHttpRequest request) {
		return publicRoutes.stream().anyMatch(route -> route.matches(request));
	}

	public String actionOf(final ServerHttpRequest request) {
		return sensitiveActions.stream()
				.filter(action -> action.route().matches(request))
				.map(NamedRoute::name)
				.findFirst()
				.orElse(TextHelper.getDefaultValue());
	}

	public boolean isAdministrative(final ServerHttpRequest request) {
		return matchesAny(administrativePaths, request);
	}

	public boolean isInternal(final ServerHttpRequest request) {
		return matchesAny(internalPaths, request);
	}

	private static boolean matchesAny(final List<PathPattern> patterns, final ServerHttpRequest request) {
		final PathContainer path = request.getPath().pathWithinApplication();
		return patterns.stream().anyMatch(pattern -> pattern.matches(path));
	}

	private static List<PathPattern> patternsOf(final List<String> paths) {
		return paths.stream()
				.map(path -> PathPatternParser.defaultInstance.parse(TextHelper.getDefaultValue(path)))
				.toList();
	}

	private static NamedRoute namedRoute(final Map.Entry<String, String> entry) {
		return new NamedRoute(TextHelper.getDefaultValue(entry.getKey()), route(entry.getValue()));
	}

	static Route route(final String rule) {

		final String cleaned = TextHelper.getDefaultValue(rule);
		final int space = cleaned.indexOf(SPACE);

		if (space < 0) {
			return new Route(ANY_METHOD, PathPatternParser.defaultInstance.parse(cleaned));
		}

		return new Route(
				cleaned.substring(0, space).trim().toUpperCase(Locale.ROOT),
				PathPatternParser.defaultInstance.parse(cleaned.substring(space + 1).trim()));
	}

	record Route(String method, PathPattern path) {

		boolean matches(final ServerHttpRequest request) {
			final boolean methodMatches = ANY_METHOD.equals(method) || method.equals(request.getMethod().name());

			return methodMatches && path.matches(request.getPath().pathWithinApplication());
		}

	}

	record NamedRoute(String name, Route route) {
	}

}
