package co.com.bookhive.gateway.services.input.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.gateway.domain.attempt.AttemptRequest;
import co.com.bookhive.gateway.domain.attempt.GuardVerdict;
import co.com.bookhive.gateway.services.input.dto.AttemptDTO;
import co.com.bookhive.gateway.services.input.dto.VerdictDTO;
import co.com.bookhive.gateway.services.usecase.guardattempts.GuardAttemptsUseCase;

class GuardAttemptsInputAdapterTest {

	@Test
	@DisplayName("convierte el DTO en dominio y el veredicto en DTO")
	void mapsBothWays() {
		final List<AttemptRequest> received = new ArrayList<>();
		final GuardAttemptsInputAdapter inputAdapter = new GuardAttemptsInputAdapter(useCase(received, GuardVerdict.block(90)));

		final VerdictDTO verdict = inputAdapter
				.admit(new AttemptDTO(" prestar ", "uid-1", "10.0.0.1", false))
				.toCompletableFuture().join();

		assertTrue(verdict.blocked());
		assertFalse(verdict.admitted());
		assertEquals(90, verdict.retryAfterSeconds());
		assertEquals(new AttemptRequest("prestar", "uid-1", "10.0.0.1", false), received.get(0));
	}

	@Test
	@DisplayName("un DTO vacío llega como solicitud vacía y un veredicto de no disponible se conserva")
	void emptyDtoAndUnavailableVerdict() {
		final List<AttemptRequest> received = new ArrayList<>();
		final GuardAttemptsInputAdapter inputAdapter = new GuardAttemptsInputAdapter(useCase(received, GuardVerdict.unavailable()));

		final VerdictDTO verdict = inputAdapter.admit(null).toCompletableFuture().join();
		inputAdapter.recordOutcome(null, 401).toCompletableFuture().join();

		assertTrue(verdict.unavailable());
		assertFalse(verdict.blocked());
		assertFalse(received.get(0).needsGuard());
		assertEquals(2, received.size());
	}

	private static GuardAttemptsUseCase useCase(final List<AttemptRequest> received, final GuardVerdict verdict) {
		return new GuardAttemptsUseCase() {

			@Override
			public CompletionStage<GuardVerdict> admit(final AttemptRequest request) {
				received.add(request);
				return CompletableFuture.completedFuture(verdict);
			}

			@Override
			public CompletionStage<Void> recordOutcome(final AttemptRequest request, final int status) {
				received.add(request);
				return CompletableFuture.completedFuture(null);
			}

		};
	}

}
