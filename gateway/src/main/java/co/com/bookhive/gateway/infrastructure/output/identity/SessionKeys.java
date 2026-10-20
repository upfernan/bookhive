package co.com.bookhive.gateway.infrastructure.output.identity;

import java.security.interfaces.RSAPublicKey;

import reactor.core.publisher.Mono;

public interface SessionKeys {

	Mono<RSAPublicKey> find(String keyId);

}
