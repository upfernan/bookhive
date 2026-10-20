package co.com.bookhive.gateway.infrastructure.output.attempts;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import co.com.bookhive.gateway.services.output.ports.AttemptStore;
import reactor.core.publisher.Mono;

@Component
public class RedisAttemptStore implements AttemptStore {

	static final RedisScript<Long> COUNT_FAILURE = RedisScript.of(
			"local failures = redis.call('INCR', KEYS[1]) "
					+ "if failures == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end "
					+ "return failures",
			Long.class);

	private static final String BLOCKED = "1";
	private static final Logger logger = LoggerFactory.getLogger(RedisAttemptStore.class);

	private final ReactiveStringRedisTemplate redis;

	public RedisAttemptStore(final ReactiveStringRedisTemplate redis) {
		super();
		this.redis = redis;
	}

	@Override
	public CompletionStage<Long> countFailure(final String counterKey, final Duration window) {
		return warnOnError(redis.execute(COUNT_FAILURE, List.of(counterKey), List.of(String.valueOf(window.toMillis())))
				.next()
				.defaultIfEmpty(0L))
				.toFuture();
	}

	@Override
	public CompletionStage<Long> remainingBlockSeconds(final String blockKey) {
		return warnOnError(redis.getExpire(blockKey)
				.map(remaining -> Math.max(0L, remaining.getSeconds()))
				.defaultIfEmpty(0L))
				.toFuture();
	}

	@Override
	public CompletionStage<Void> block(final String blockKey, final Duration duration) {
		return warnOnError(redis.opsForValue().set(blockKey, BLOCKED, duration).then()).toFuture();
	}

	@Override
	public CompletionStage<Void> forget(final String key) {
		return warnOnError(redis.delete(key).then()).toFuture();
	}

	private static <T> Mono<T> warnOnError(final Mono<T> operation) {
		return operation.doOnError(failure -> logger.warn("Redis no respondió: {}", failure.getMessage()));
	}

}
