package co.com.bookhive.gateway.initializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import co.com.bookhive.gateway.infrastructure.input.filters.BackendIdentityFilter;
import co.com.bookhive.gateway.infrastructure.input.filters.EntryGuardFilter;
import co.com.bookhive.gateway.services.input.ports.GuardAttemptsInputPort;

@SpringBootTest(properties = {
		"bookhive.identity.project-id=bookhive-511618",
		"spring.data.redis.host=127.0.0.1",
		"spring.data.redis.port=1" })
class GatewayApplicationTests {

	@Autowired
	private EntryGuardFilter entryGuard;

	@Autowired
	private BackendIdentityFilter backendIdentity;

	@Autowired
	private GuardAttemptsInputPort guard;

	@Test
	@DisplayName("la puerta arranca sin Redis y con los filtros conectados")
	void contextStartsWithoutRedis() {
		assertNotNull(entryGuard);
		assertNotNull(backendIdentity);
		assertNotNull(guard);
	}

}
