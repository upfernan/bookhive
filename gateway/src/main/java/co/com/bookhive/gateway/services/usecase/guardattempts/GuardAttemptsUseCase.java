package co.com.bookhive.gateway.services.usecase.guardattempts;

import java.util.concurrent.CompletionStage;

import co.com.bookhive.gateway.domain.attempt.AttemptRequest;
import co.com.bookhive.gateway.domain.attempt.GuardVerdict;

public interface GuardAttemptsUseCase {

	CompletionStage<GuardVerdict> admit(AttemptRequest request);

	CompletionStage<Void> recordOutcome(AttemptRequest request, int status);

}
