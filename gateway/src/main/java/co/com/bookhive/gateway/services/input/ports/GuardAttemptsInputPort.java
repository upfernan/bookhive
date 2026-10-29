package co.com.bookhive.gateway.services.input.ports;

import java.util.concurrent.CompletionStage;
import co.com.bookhive.gateway.services.input.dto.AttemptDTO;
import co.com.bookhive.gateway.services.input.dto.VerdictDTO;

public interface GuardAttemptsInputPort {

	CompletionStage<VerdictDTO> admit(AttemptDTO attempt);

	CompletionStage<Void> recordOutcome(AttemptDTO attempt, int status);

}
