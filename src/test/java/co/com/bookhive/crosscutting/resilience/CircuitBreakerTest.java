package co.com.bookhive.crosscutting.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest {

	private static final Duration OPEN_FOR = Duration.ofSeconds(30);

	private final MovableClock clock = new MovableClock();
	private final List<String> changes = new ArrayList<>();
	private final AtomicInteger calls = new AtomicInteger();

	@Test
	@DisplayName("deja pasar las llamadas mientras el servicio responde")
	void staysClosedWhileServiceAnswers() {
		final CircuitBreaker breaker = breaker();

		assertEquals("ok", breaker.call(this::answer));
		assertEquals(CircuitBreaker.State.CLOSED, breaker.state());
		assertEquals(1, calls.get());
	}

	@Test
	@DisplayName("después de los fallos seguidos deja de llamar al servicio y responde de inmediato")
	void opensAfterConsecutiveFailures() {
		final CircuitBreaker breaker = breaker();

		failTimes(breaker, 3);

		assertEquals(CircuitBreaker.State.OPEN, breaker.state());
		assertThrows(CircuitOpenException.class, () -> breaker.call(this::answer));
		assertEquals(3, calls.get());
		assertEquals(1, changes.size());
	}

	@Test
	@DisplayName("pasado el corte prueba una llamada y, si responde, vuelve a la normalidad")
	void closesAgainWhenTrialCallWorks() {
		final CircuitBreaker breaker = breaker();
		failTimes(breaker, 3);

		clock.move(OPEN_FOR);

		assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.state());
		assertEquals("ok", breaker.call(this::answer));
		assertEquals(CircuitBreaker.State.CLOSED, breaker.state());
	}

	@Test
	@DisplayName("si la llamada de prueba falla, vuelve a cortar")
	void reopensWhenTrialCallFails() {
		final CircuitBreaker breaker = breaker();
		failTimes(breaker, 3);
		clock.move(OPEN_FOR);

		failTimes(breaker, 1);

		assertEquals(CircuitBreaker.State.OPEN, breaker.state());
	}

	@Test
	@DisplayName("un éxito reinicia la cuenta de fallos seguidos")
	void successResetsFailures() {
		final CircuitBreaker breaker = breaker();

		failTimes(breaker, 2);
		breaker.call(this::answer);
		failTimes(breaker, 2);

		assertEquals(CircuitBreaker.State.CLOSED, breaker.state());
	}

	@Test
	@DisplayName("con datos nulos usa valores seguros")
	void usesSafeDefaults() {
		final CircuitBreaker breaker = new CircuitBreaker(null, 0, null, null, null);

		failTimes(breaker, 1);

		assertEquals(CircuitBreaker.State.OPEN, breaker.state());
	}

	private CircuitBreaker breaker() {
		return new CircuitBreaker("Wompi", 3, OPEN_FOR, clock, changes::add);
	}

	private String answer() {
		calls.incrementAndGet();
		return "ok";
	}

	private void failTimes(final CircuitBreaker breaker, final int times) {
		for (int attempt = 0; attempt < times; attempt++) {
			assertThrows(IllegalStateException.class, () -> breaker.call(() -> {
				calls.incrementAndGet();
				throw new IllegalStateException("caído");
			}));
		}
	}

	private static final class MovableClock extends Clock {

		private Instant now = Instant.parse("2026-10-26T12:00:00Z");

		void move(final Duration duration) {
			now = now.plus(duration);
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(final ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}

	}

}
