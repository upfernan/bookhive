package co.com.bookhive.shared.infrastructure.output.messaging.pubsub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.testdata.FakeHttpServer;

class GoogleAccessTokensTest {

	private static final Instant NOW = Instant.parse("2026-10-22T12:00:00Z");

	@Test
	@DisplayName("reutiliza el pase mientras no esté por vencer y lo renueva un minuto antes")
	void reusesAndRenewsToken() throws IOException {
		try (FakeHttpServer server = new FakeHttpServer()) {
			server.answer("GET", "/token", 200, "{\"access_token\":\"pase-1\",\"expires_in\":120}")
					.answer("GET", "/token", 200, "{\"access_token\":\"pase-2\",\"expires_in\":3600}");
			final MovableClock clock = new MovableClock(NOW);
			final GoogleAccessTokens tokens = new GoogleAccessTokens(server.url() + "/token", clock);

			assertTrue(tokens.isRequired());
			assertEquals("pase-1", tokens.current());
			assertEquals("pase-1", tokens.current());
			clock.now = NOW.plusSeconds(61);
			assertEquals("pase-2", tokens.current());
			assertEquals(2, server.requests().size());
		}
	}

	@Test
	@DisplayName("si el servidor de metadatos falla o no entrega pase, avisa que la mensajería no está disponible")
	void reportsFailures() throws IOException {
		try (FakeHttpServer server = new FakeHttpServer()) {
			server.answer("GET", "/token", 500, "")
					.answer("GET", "/token", 200, "{\"expires_in\":3600}")
					.answer("GET", "/token", 200, "no es json");
			final GoogleAccessTokens tokens = new GoogleAccessTokens(server.url() + "/token", Clock.systemUTC());

			for (int attempt = 0; attempt < 3; attempt++) {
				final BookHiveException exception = assertThrows(BookHiveException.class, tokens::current);
				assertEquals(MessageKey.MESSAGING_UNAVAILABLE, exception.getUserMessageKey());
			}
		}
		assertFalse(new GoogleAccessTokens("", Clock.systemUTC()).isRequired());
		final GoogleAccessTokens unreachable = new GoogleAccessTokens("http://127.0.0.1:1/token", Clock.systemUTC());
		assertThrows(BookHiveException.class, unreachable::current);
	}

	private static final class MovableClock extends Clock {

		private Instant now;

		private MovableClock(final Instant now) {
			this.now = now;
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(final java.time.ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}

	}

}
