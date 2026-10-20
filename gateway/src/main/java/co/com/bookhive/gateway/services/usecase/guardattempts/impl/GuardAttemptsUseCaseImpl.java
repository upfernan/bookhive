package co.com.bookhive.gateway.services.usecase.guardattempts.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.LongPredicate;

import co.com.bookhive.gateway.domain.attempt.AttemptKeys;
import co.com.bookhive.gateway.domain.attempt.AttemptPolicy;
import co.com.bookhive.gateway.domain.attempt.AttemptRequest;
import co.com.bookhive.gateway.domain.attempt.GuardVerdict;
import co.com.bookhive.gateway.services.output.ports.AttemptStore;
import co.com.bookhive.gateway.services.output.ports.AuditPort;
import co.com.bookhive.gateway.services.usecase.guardattempts.GuardAttemptsUseCase;

public final class GuardAttemptsUseCaseImpl implements GuardAttemptsUseCase {

	private static final CompletionStage<Void> DONE = CompletableFuture.completedFuture(null);
	private static final String ACTION = "accion ";

	private final AttemptPolicy policy;
	private final AttemptStore store;
	private final AuditPort audit;

	public GuardAttemptsUseCaseImpl(final AttemptPolicy policy, final AttemptStore store, final AuditPort audit) {
		super();
		this.policy = policy == null ? AttemptPolicy.byDefault() : policy;
		this.store = store;
		this.audit = audit;
	}

	@Override
	public CompletionStage<GuardVerdict> admit(final AttemptRequest request) {

		if (!request.needsGuard()) {
			return CompletableFuture.completedFuture(GuardVerdict.admit());
		}

		return remainingBlockSeconds(request).handle((seconds, failure) -> {
			if (failure != null) {
				return request.administrative() ? GuardVerdict.unavailable() : GuardVerdict.admit();
			}

			if (seconds != null && seconds > 0) {
				audit.register(AuditPort.REQUEST_HELD, request.account(), request.origin(),
						ACTION + request.action() + " espera " + seconds + "s");
				return GuardVerdict.block(seconds);
			}

			return GuardVerdict.admit();
		});
	}

	@Override
	public CompletionStage<Void> recordOutcome(final AttemptRequest request, final int status) {

		if (!request.isSensitive() || !policy.isFailure(status)) {
			return DONE;
		}

		audit.register(AuditPort.FAILED_ATTEMPT, request.account(), request.origin(),
				ACTION + request.action() + " estado " + status);

		final CompletionStage<Void> byAccount = request.hasAccount()
				? countAndHold(AttemptKeys.accountCounter(request), AttemptKeys.accountBlock(request),
						policy::accountReachedLimit, AuditPort.ACCOUNT_HELD, request)
				: DONE;

		final CompletionStage<Void> byOrigin = countAndHold(AttemptKeys.originCounter(request),
				AttemptKeys.originBlock(request), policy::originReachedLimit, AuditPort.ORIGIN_HELD, request);

		return byAccount
				.thenCombine(byOrigin, (accountDone, originDone) -> accountDone)
				.exceptionally(failure -> null);
	}

	private CompletionStage<Long> remainingBlockSeconds(final AttemptRequest request) {

		final CompletionStage<Long> byOrigin = store.remainingBlockSeconds(AttemptKeys.originBlock(request));

		if (!request.hasAccount()) {
			return byOrigin;
		}

		return byOrigin.thenCombine(store.remainingBlockSeconds(AttemptKeys.accountBlock(request)), Math::max);
	}

	private CompletionStage<Void> countAndHold(final String counterKey, final String blockKey,
			final LongPredicate reachedLimit, final String auditAction, final AttemptRequest request) {

		return store.countFailure(counterKey, policy.window()).thenCompose(failures -> {
			if (!reachedLimit.test(failures)) {
				return DONE;
			}

			audit.register(auditAction, request.account(), request.origin(),
					ACTION + request.action() + " intentos " + failures + " bloqueo " + policy.blockFor().toMinutes() + "min");

			return store.block(blockKey, policy.blockFor()).thenCompose(blocked -> store.forget(counterKey));
		});
	}

}
