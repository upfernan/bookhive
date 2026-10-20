package co.com.bookhive.gateway.services.output.ports;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public interface AttemptStore {

	CompletionStage<Long> countFailure(String counterKey, Duration window);

	CompletionStage<Long> remainingBlockSeconds(String blockKey);

	CompletionStage<Void> block(String blockKey, Duration duration);

	CompletionStage<Void> forget(String key);

}
