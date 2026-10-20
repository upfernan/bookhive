package co.com.bookhive.gateway.infrastructure.input.security;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.support.ipresolver.RemoteAddressResolver;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import co.com.bookhive.gateway.crosscutting.helper.TextHelper;

@Component
public class ClientOrigin {

	static final String UNKNOWN = "desconocido";

	private final RemoteAddressResolver resolver;

	public ClientOrigin(@Value("${bookhive.gateway.trusted-hops:1}") final int trustedHops) {
		super();
		this.resolver = XForwardedRemoteAddressResolver.maxTrustedIndex(Math.max(1, trustedHops));
	}

	public String of(final ServerWebExchange exchange) {

		final InetSocketAddress address = resolver.resolve(exchange);

		if (address == null) {
			return UNKNOWN;
		}

		final InetAddress resolved = address.getAddress();

		return resolved == null
				? TextHelper.getDefaultValue(address.getHostString(), UNKNOWN)
				: resolved.getHostAddress();
	}

}
