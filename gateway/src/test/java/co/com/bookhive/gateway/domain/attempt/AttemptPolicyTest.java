package co.com.bookhive.gateway.domain.attempt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttemptPolicyTest {

	@Test
	@DisplayName("por defecto: 5 intentos por cuenta, 100 por origen, en 10 minutos y bloqueo de 10 minutos (ADR-031)")
	void defaultsFollowAdr() {
		final AttemptPolicy policy = AttemptPolicy.byDefault();

		assertEquals(5, policy.maxPerAccount());
		assertEquals(100, policy.maxPerOrigin());
		assertEquals(Duration.ofMinutes(10), policy.window());
		assertEquals(Duration.ofMinutes(10), policy.blockFor());
	}

	@Test
	@DisplayName("los valores vacíos o inválidos toman los valores por defecto")
	void invalidValuesFallBackToDefaults() {
		final AttemptPolicy policy = new AttemptPolicy(0, -1, null, Duration.ZERO);

		assertEquals(AttemptPolicy.byDefault(), policy);
	}

	@Test
	@DisplayName("el límite por origen nunca queda por debajo del límite por cuenta")
	void originLimitIsNeverLowerThanAccountLimit() {
		final AttemptPolicy policy = new AttemptPolicy(5, 2, Duration.ofMinutes(1), Duration.ofMinutes(1));

		assertEquals(5, policy.maxPerOrigin());
	}

	@Test
	@DisplayName("solo 401 y 403 cuentan como intento fallido")
	void onlyUnauthorizedAndForbiddenAreFailures() {
		final AttemptPolicy policy = AttemptPolicy.byDefault();

		assertTrue(policy.isFailure(401));
		assertTrue(policy.isFailure(403));
		assertFalse(policy.isFailure(200));
		assertFalse(policy.isFailure(409));
		assertFalse(policy.isFailure(500));
	}

	@Test
	@DisplayName("el bloqueo se activa al llegar al límite, no antes")
	void limitIsReachedAtTheMaximum() {
		final AttemptPolicy policy = AttemptPolicy.byDefault();

		assertFalse(policy.accountReachedLimit(4));
		assertTrue(policy.accountReachedLimit(5));
		assertFalse(policy.originReachedLimit(99));
		assertTrue(policy.originReachedLimit(100));
	}

}
