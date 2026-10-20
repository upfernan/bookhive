package co.com.bookhive.gateway.infrastructure.output.attempts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class RedisAttemptStoreTest {

	private static final String COUNTER = "bookhive:intentos:cuenta:uid-1:prestar";
	private static final String BLOCK = "bookhive:bloqueo:cuenta:uid-1:prestar";

	private final ReactiveStringRedisTemplate redis = mock(ReactiveStringRedisTemplate.class);
	private final RedisAttemptStore store = new RedisAttemptStore(redis);

	@Test
	@DisplayName("cuenta y fija el vencimiento en un solo paso, con la ventana en milisegundos")
	void countsWithScript() {
		when(redis.execute(eq(RedisAttemptStore.COUNT_FAILURE), eq(List.of(COUNTER)), eq(List.of("600000"))))
				.thenReturn(Flux.just(3L));

		assertEquals(3L, store.countFailure(COUNTER, Duration.ofMinutes(10)).toCompletableFuture().join());
	}

	@Test
	@DisplayName("el script incrementa y solo pone vencimiento la primera vez")
	void scriptIncrementsAndExpiresOnce() {
		final String script = RedisAttemptStore.COUNT_FAILURE.getScriptAsString();

		assertEquals(Long.class, RedisAttemptStore.COUNT_FAILURE.getResultType());
		assertTrue(script.contains("INCR") && script.contains("PEXPIRE") && script.contains("failures == 1"));
	}

	@Test
	@DisplayName("la espera restante nunca es negativa: sin llave o sin vencimiento es cero")
	void remainingBlockIsNeverNegative() {
		when(redis.getExpire(BLOCK)).thenReturn(Mono.just(Duration.ofSeconds(-2)), Mono.just(Duration.ofSeconds(240)), Mono.empty());

		assertEquals(0L, store.remainingBlockSeconds(BLOCK).toCompletableFuture().join());
		assertEquals(240L, store.remainingBlockSeconds(BLOCK).toCompletableFuture().join());
		assertEquals(0L, store.remainingBlockSeconds(BLOCK).toCompletableFuture().join());
	}

	@Test
	@DisplayName("bloquear guarda la llave con su vencimiento y olvidar la borra")
	@SuppressWarnings("unchecked")
	void blocksAndForgets() {
		final ReactiveValueOperations<String, String> values = mock(ReactiveValueOperations.class);
		when(redis.opsForValue()).thenReturn(values);
		when(values.set(BLOCK, "1", Duration.ofMinutes(10))).thenReturn(Mono.just(true));
		when(redis.delete(COUNTER)).thenReturn(Mono.just(1L));

		store.block(BLOCK, Duration.ofMinutes(10)).toCompletableFuture().join();
		store.forget(COUNTER).toCompletableFuture().join();

		verify(values).set(BLOCK, "1", Duration.ofMinutes(10));
		verify(redis).delete(COUNTER);
	}

	@Test
	@DisplayName("si Redis no responde, el error se entrega para que el caso de uso decida")
	void failuresArePropagated() {
		when(redis.execute(eq(RedisAttemptStore.COUNT_FAILURE), anyList(), anyList()))
				.thenReturn(Flux.error(new IllegalStateException("Redis caído")));

		assertThrows(CompletionException.class,
				() -> store.countFailure(COUNTER, Duration.ofMinutes(10)).toCompletableFuture().join());
	}

}
