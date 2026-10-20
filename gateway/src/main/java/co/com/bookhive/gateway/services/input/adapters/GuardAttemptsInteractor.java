package co.com.bookhive.gateway.services.input.adapters;

import java.util.concurrent.CompletionStage;

import co.com.bookhive.gateway.services.input.adapters.mapper.AttemptMapper;
import co.com.bookhive.gateway.services.input.dto.AttemptDTO;
import co.com.bookhive.gateway.services.input.dto.VerdictDTO;
import co.com.bookhive.gateway.services.input.ports.GuardAttemptsInputPort;
import co.com.bookhive.gateway.services.usecase.guardattempts.GuardAttemptsUseCase;

public final class GuardAttemptsInteractor implements GuardAttemptsInputPort {

	private final GuardAttemptsUseCase useCase;

	public GuardAttemptsInteractor(final GuardAttemptsUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public CompletionStage<VerdictDTO> admit(final AttemptDTO attempt) {
		return useCase.admit(AttemptMapper.toDomain(attempt)).thenApply(AttemptMapper::toDto);
	}

	@Override
	public CompletionStage<Void> recordOutcome(final AttemptDTO attempt, final int status) {
		return useCase.recordOutcome(AttemptMapper.toDomain(attempt), status);
	}

}
