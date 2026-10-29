package co.com.bookhive.gateway.infrastructure.input.filters;

import org.springframework.boot.context.properties.ConfigurationProperties;
import co.com.bookhive.gateway.crosscutting.helper.TextHelper;

@ConfigurationProperties(prefix = "bookhive.gateway.messages")
public record GatewayMessagesProperties(String withoutSession, String blocked, String unavailable) {

	static final String DEFAULT_WITHOUT_SESSION = "Tu sesión no es válida o ya se cerró. Vuelve a iniciar sesión.";
	static final String DEFAULT_BLOCKED = "Hiciste demasiados intentos seguidos. Espera unos minutos y vuelve a intentarlo.";
	static final String DEFAULT_UNAVAILABLE = "Esta operación no está disponible en este momento. Inténtalo más tarde.";

	public GatewayMessagesProperties {
		withoutSession = TextHelper.getDefaultValue(withoutSession, DEFAULT_WITHOUT_SESSION);
		blocked = TextHelper.getDefaultValue(blocked, DEFAULT_BLOCKED);
		unavailable = TextHelper.getDefaultValue(unavailable, DEFAULT_UNAVAILABLE);
	}

}
