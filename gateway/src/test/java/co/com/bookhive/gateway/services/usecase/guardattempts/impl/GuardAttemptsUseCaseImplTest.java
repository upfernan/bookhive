package co.com.bookhive.gateway.services.usecase.guardattempts.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.gateway.domain.attempt.AttemptKeys;
import co.com.bookhive.gateway.domain.attempt.AttemptPolicy;
import co.com.bookhive.gateway.domain.attempt.AttemptRequest;
import co.com.bookhive.gateway.domain.attempt.GuardVerdict;
import co.com.bookhive.gateway.services.output.ports.AuditPort;
import co.com.bookhive.gateway.testdata.FakeAttemptStore;
import co.com.bookhive.gateway.testdata.RecordingAudit;

class GuardAttemptsUseCaseImplTest {

	private static final AttemptRequest LEND = new AttemptRequest("prestar", "uid-1", "200.9.159.10", false);
	private static final AttemptRequest LEND_WITHOUT_ACCOUNT = new AttemptRequest("prestar", "", "200.9.159.10", false);
	private static final AttemptRequest ORDINARY = new AttemptRequest("", "uid-1", "200.9.159.10", false);
	private static final AttemptRequest ADMINISTRATIVE = new AttemptRequest("", "uid-1", "200.9.159.10", true);

	private final RecordingAudit audit = new RecordingAudit();

	private GuardAttemptsUseCaseImpl useCaseWith(final FakeAttemptStore store) {
		return new GuardAttemptsUseCaseImpl(AttemptPolicy.byDefault(), store, audit);
	}

	private static <T> T await(final CompletionStage<T> stage) {
		return stage.toCompletableFuture().join();
	}

	@Test
	@DisplayName("una solicitud común pasa sin preguntarle a Redis")
	void ordinaryRequestPassesWithoutStore() {
		final GuardVerdict verdict = await(useCaseWith(FakeAttemptStore.unreachable()).admit(ORDINARY));

		assertEquals(GuardVerdict.admit(), verdict);
	}

	@Test
	@DisplayName("una acción sensible sin bloqueos pasa")
	void sensitiveActionWithoutBlocksPasses() {
		final GuardVerdict verdict = await(useCaseWith(FakeAttemptStore.working()).admit(LEND));

		assertEquals(GuardVerdict.Decision.ADMIT, verdict.decision());
	}

	@Test
	@DisplayName("si la cuenta está bloqueada para esa acción, se frena y queda registro")
	void blockedAccountIsHeld() {
		final FakeAttemptStore store = FakeAttemptStore.working().withBlock(AttemptKeys.accountBlock(LEND), 420);

		final GuardVerdict verdict = await(useCaseWith(store).admit(LEND));

		assertEquals(GuardVerdict.Decision.BLOCK, verdict.decision());
		assertEquals(420, verdict.retryAfterSeconds());
		assertEquals(List.of(AuditPort.REQUEST_HELD), audit.actions());
	}

	@Test
	@DisplayName("si el origen está bloqueado, se frena aunque la cuenta no lo esté, con la espera más larga")
	void blockedOriginIsHeld() {
		final FakeAttemptStore store = FakeAttemptStore.working()
				.withBlock(AttemptKeys.originBlock(LEND), 600)
				.withBlock(AttemptKeys.accountBlock(LEND), 60);

		final GuardVerdict verdict = await(useCaseWith(store).admit(LEND));

		assertEquals(600, verdict.retryAfterSeconds());
	}

	@Test
	@DisplayName("el bloqueo cae solo sobre la acción que venía fallando")
	void blockOnlyAppliesToItsAction() {
		final FakeAttemptStore store = FakeAttemptStore.working().withBlock(AttemptKeys.accountBlock(LEND), 600);
		final AttemptRequest otherAction = new AttemptRequest("abrir-sesion", "uid-1", "200.9.159.10", false);

		final GuardVerdict verdict = await(useCaseWith(store).admit(otherAction));

		assertEquals(GuardVerdict.Decision.ADMIT, verdict.decision());
	}

	@Test
	@DisplayName("si Redis no responde, el tráfico normal pasa")
	void storeDownLetsNormalTrafficPass() {
		final GuardVerdict verdict = await(useCaseWith(FakeAttemptStore.unreachable()).admit(LEND));

		assertEquals(GuardVerdict.Decision.ADMIT, verdict.decision());
	}

	@Test
	@DisplayName("si Redis no responde, las operaciones administrativas quedan bloqueadas (ADR-031)")
	void storeDownHoldsAdministrativeOperations() {
		final GuardVerdict verdict = await(useCaseWith(FakeAttemptStore.unreachable()).admit(ADMINISTRATIVE));

		assertEquals(GuardVerdict.Decision.UNAVAILABLE, verdict.decision());
	}

	@Test
	@DisplayName("una operación administrativa pasa cuando Redis responde")
	void administrativeOperationPassesWhenStoreIsUp() {
		final GuardVerdict verdict = await(useCaseWith(FakeAttemptStore.working()).admit(ADMINISTRATIVE));

		assertEquals(GuardVerdict.Decision.ADMIT, verdict.decision());
	}

	@Test
	@DisplayName("un intento fallido suma en la cuenta y en el origen, y queda registrado")
	void failureCountsAccountAndOrigin() {
		final FakeAttemptStore store = FakeAttemptStore.working();

		await(useCaseWith(store).recordOutcome(LEND, 403));

		assertEquals(1, store.failuresOf(AttemptKeys.accountCounter(LEND)));
		assertEquals(1, store.failuresOf(AttemptKeys.originCounter(LEND)));
		assertEquals(List.of(AuditPort.FAILED_ATTEMPT), audit.actions());
		assertTrue(audit.events().get(0).contains("accion prestar estado 403"));
	}

	@Test
	@DisplayName("sin cuenta solo suma el origen")
	void failureWithoutAccountOnlyCountsOrigin() {
		final FakeAttemptStore store = FakeAttemptStore.working();

		await(useCaseWith(store).recordOutcome(LEND_WITHOUT_ACCOUNT, 401));

		assertEquals(0, store.failuresOf(AttemptKeys.accountCounter(LEND_WITHOUT_ACCOUNT)));
		assertEquals(1, store.failuresOf(AttemptKeys.originCounter(LEND_WITHOUT_ACCOUNT)));
	}

	@Test
	@DisplayName("al quinto intento fallido la cuenta queda frenada 10 minutos y el contador vuelve a cero")
	void fifthFailureBlocksTheAccount() {
		final FakeAttemptStore store = FakeAttemptStore.working()
				.withFailures(AttemptKeys.accountCounter(LEND), 4);

		await(useCaseWith(store).recordOutcome(LEND, 403));

		assertEquals(600, store.blockOf(AttemptKeys.accountBlock(LEND)));
		assertEquals(0, store.failuresOf(AttemptKeys.accountCounter(LEND)));
		assertEquals(0, store.blockOf(AttemptKeys.originBlock(LEND)));
		assertEquals(List.of(AuditPort.FAILED_ATTEMPT, AuditPort.ACCOUNT_HELD), audit.actions());
	}

	@Test
	@DisplayName("el origen se frena solo con un número mucho más alto")
	void originNeedsManyMoreFailures() {
		final FakeAttemptStore store = FakeAttemptStore.working()
				.withFailures(AttemptKeys.originCounter(LEND_WITHOUT_ACCOUNT), 99);

		await(useCaseWith(store).recordOutcome(LEND_WITHOUT_ACCOUNT, 401));

		assertEquals(600, store.blockOf(AttemptKeys.originBlock(LEND_WITHOUT_ACCOUNT)));
		assertEquals(List.of(AuditPort.FAILED_ATTEMPT, AuditPort.ORIGIN_HELD), audit.actions());
	}

	@Test
	@DisplayName("las respuestas exitosas o de negocio no cuentan como intento")
	void successAndBusinessErrorsDoNotCount() {
		final FakeAttemptStore store = FakeAttemptStore.working();
		final GuardAttemptsUseCaseImpl useCase = useCaseWith(store);

		await(useCase.recordOutcome(LEND, 201));
		await(useCase.recordOutcome(LEND, 409));
		await(useCase.recordOutcome(ORDINARY, 403));

		assertTrue(store.events().isEmpty());
		assertTrue(audit.events().isEmpty());
	}

	@Test
	@DisplayName("si Redis no responde al contar, el intento igual queda registrado y nada se rompe")
	void storeDownWhileCountingStillAudits() {
		await(useCaseWith(FakeAttemptStore.unreachable()).recordOutcome(LEND, 403));

		assertEquals(List.of(AuditPort.FAILED_ATTEMPT), audit.actions());
	}

	@Test
	@DisplayName("sin política usa la de ADR-031")
	void missingPolicyUsesDefaults() {
		final FakeAttemptStore store = FakeAttemptStore.working()
				.withFailures(AttemptKeys.accountCounter(LEND), 4);

		await(new GuardAttemptsUseCaseImpl(null, store, audit).recordOutcome(LEND, 401));

		assertEquals(600, store.blockOf(AttemptKeys.accountBlock(LEND)));
	}

}
