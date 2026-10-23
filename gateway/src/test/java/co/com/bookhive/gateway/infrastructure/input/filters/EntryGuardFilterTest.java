package co.com.bookhive.gateway.infrastructure.input.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import co.com.bookhive.gateway.infrastructure.input.rules.EntryRules;
import co.com.bookhive.gateway.infrastructure.input.security.ClientOrigin;
import co.com.bookhive.gateway.infrastructure.input.security.SessionCookieVerifier;
import co.com.bookhive.gateway.services.input.dto.AttemptDTO;
import co.com.bookhive.gateway.services.input.dto.VerdictDTO;
import co.com.bookhive.gateway.services.input.ports.GuardAttemptsInputPort;
import co.com.bookhive.gateway.testdata.GatewayRulesData;
import co.com.bookhive.gateway.testdata.SessionTokens;
import reactor.core.publisher.Mono;

class EntryGuardFilterTest {

	private static final VerdictDTO ADMIT = new VerdictDTO(true, false, 0L);

	private final RecordingGuard guard = new RecordingGuard();
	private final AtomicBoolean forwarded = new AtomicBoolean();

	private EntryGuardFilter filterAnswering(final VerdictDTO verdict) {
		guard.verdict = verdict;
		final SessionCookieVerifier sessions = new SessionCookieVerifier(SessionTokens.PROJECT,
				keyId -> Mono.just(SessionTokens.publicKey()), Clock.fixed(SessionTokens.NOW, ZoneOffset.UTC));

		return new EntryGuardFilter(guard, new EntryRules(GatewayRulesData.PROPERTIES), sessions, new ClientOrigin(1),
				new GatewayResponses(new GatewayMessagesProperties(null, null, null)));
	}

	private GatewayFilterChain backendAnswering(final HttpStatus status) {
		return exchange -> {
			forwarded.set(true);
			exchange.getResponse().setStatusCode(status);
			return Mono.empty();
		};
	}

	private static MockServerWebExchange lendWithSession() {
		return MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/prestamos")
				.header("X-Forwarded-For", "191.91.85.226")
				.cookie(new HttpCookie(SessionCookieVerifier.SESSION_COOKIE, SessionTokens.valid())));
	}

	@Test
	@DisplayName("con sesión válida pasa al backend, y un 403 queda contado para esa cuenta y esa acción")
	void validSessionIsForwardedAndForbiddenIsRecorded() {
		final MockServerWebExchange exchange = lendWithSession();

		filterAnswering(ADMIT).filter(exchange, backendAnswering(HttpStatus.FORBIDDEN)).block();

		assertTrue(forwarded.get());
		assertEquals(new AttemptDTO("prestar", SessionTokens.ACCOUNT, "191.91.85.226", false), guard.admitted.get(0));
		assertEquals(List.of(403), guard.statuses);
	}

	@Test
	@DisplayName("las rutas internas responden 404 aunque haya sesión, sin llegar al backend ni contar intentos")
	void internalRoutesAreNeverForwarded() {
		final MockServerWebExchange exchange = MockServerWebExchange.from(
				MockServerHttpRequest.post("/api/v1/avisos/correo")
						.cookie(new HttpCookie(SessionCookieVerifier.SESSION_COOKIE, SessionTokens.valid())));

		filterAnswering(ADMIT).filter(exchange, backendAnswering(HttpStatus.NO_CONTENT)).block();

		assertFalse(forwarded.get());
		assertEquals(HttpStatus.NOT_FOUND, exchange.getResponse().getStatusCode());
		assertTrue(guard.admitted.isEmpty());
	}

	@Test
	@DisplayName("sin sesión válida en una ruta protegida responde 401 sin llegar al backend y lo cuenta")
	void protectedRouteWithoutSessionIsRejected() {
		final MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/prestamos")
				.header("X-Forwarded-For", "191.91.85.226"));

		filterAnswering(ADMIT).filter(exchange, backendAnswering(HttpStatus.CREATED)).block();

		assertFalse(forwarded.get());
		assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
		assertEquals("", guard.admitted.get(0).account());
		assertEquals(List.of(401), guard.statuses);
	}

	@Test
	@DisplayName("una ruta pública pasa sin sesión y su respuesta también se cuenta")
	void publicRoutePassesWithoutSession() {
		final MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/sesiones"));

		filterAnswering(ADMIT).filter(exchange, backendAnswering(HttpStatus.UNAUTHORIZED)).block();

		assertTrue(forwarded.get());
		assertEquals("abrir-sesion", guard.admitted.get(0).action());
		assertEquals(List.of(401), guard.statuses);
	}

	@Test
	@DisplayName("si la acción está frenada responde 429 con la espera y no llega al backend")
	void blockedRequestIsHeld() {
		final MockServerWebExchange exchange = lendWithSession();

		filterAnswering(new VerdictDTO(false, false, 300)).filter(exchange, backendAnswering(HttpStatus.CREATED)).block();

		assertFalse(forwarded.get());
		assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
		assertEquals("300", exchange.getResponse().getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
		assertTrue(guard.statuses.isEmpty());
	}

	@Test
	@DisplayName("si lo administrativo no se puede proteger responde 503")
	void unavailableAnswersServiceUnavailable() {
		final MockServerWebExchange exchange = lendWithSession();

		filterAnswering(new VerdictDTO(false, true, 0)).filter(exchange, backendAnswering(HttpStatus.CREATED)).block();

		assertFalse(forwarded.get());
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
	}

	@Test
	@DisplayName("si el guardián falla, la solicitud pasa y la respuesta sin estado cuenta como 200")
	void guardFailureLetsRequestPass() {
		final MockServerWebExchange exchange = lendWithSession();
		guard.failAdmit = true;

		filterAnswering(ADMIT).filter(exchange, ignored -> {
			forwarded.set(true);
			return Mono.empty();
		}).block();

		assertTrue(forwarded.get());
		assertEquals(List.of(200), guard.statuses);
	}

	@Test
	@DisplayName("corre antes que los demás filtros")
	void runsEarly() {
		assertEquals(EntryGuardFilter.ORDER, filterAnswering(ADMIT).getOrder());
	}

	private static final class RecordingGuard implements GuardAttemptsInputPort {

		private final List<AttemptDTO> admitted = new ArrayList<>();
		private final List<Integer> statuses = new ArrayList<>();
		private VerdictDTO verdict = ADMIT;
		private boolean failAdmit;

		@Override
		public CompletionStage<VerdictDTO> admit(final AttemptDTO attempt) {
			admitted.add(attempt);
			return failAdmit
					? CompletableFuture.failedFuture(new IllegalStateException("falla"))
					: CompletableFuture.completedFuture(verdict);
		}

		@Override
		public CompletionStage<Void> recordOutcome(final AttemptDTO attempt, final int status) {
			statuses.add(status);
			return CompletableFuture.completedFuture(null);
		}

	}

}
