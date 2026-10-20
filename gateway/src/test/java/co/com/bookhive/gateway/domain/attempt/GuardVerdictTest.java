package co.com.bookhive.gateway.domain.attempt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GuardVerdictTest {

	@Test
	@DisplayName("un bloqueo siempre pide esperar al menos un segundo")
	void blockWaitsAtLeastOneSecond() {
		assertEquals(1L, GuardVerdict.block(0).retryAfterSeconds());
		assertEquals(GuardVerdict.Decision.BLOCK, GuardVerdict.block(30).decision());
	}

	@Test
	@DisplayName("los valores vacíos se vuelven un veredicto de paso")
	void emptyVerdictAdmits() {
		final GuardVerdict verdict = new GuardVerdict(null, -5);

		assertEquals(GuardVerdict.Decision.ADMIT, verdict.decision());
		assertEquals(0L, verdict.retryAfterSeconds());
		assertEquals(GuardVerdict.Decision.UNAVAILABLE, GuardVerdict.unavailable().decision());
	}

}
