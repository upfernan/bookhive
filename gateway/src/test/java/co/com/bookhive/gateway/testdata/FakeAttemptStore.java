package co.com.bookhive.gateway.testdata;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import co.com.bookhive.gateway.services.output.ports.AttemptStore;

public final class FakeAttemptStore implements AttemptStore {

	private final Map<String, Long> counters = new HashMap<>();
	private final Map<String, Long> blocks = new HashMap<>();
	private final List<String> events = new ArrayList<>();
	private boolean down;

	public static FakeAttemptStore working() {
		return new FakeAttemptStore();
	}

	public static FakeAttemptStore unreachable() {
		final FakeAttemptStore store = new FakeAttemptStore();
		store.down = true;
		return store;
	}

	public FakeAttemptStore withBlock(final String key, final long seconds) {
		blocks.put(key, seconds);
		return this;
	}

	public FakeAttemptStore withFailures(final String key, final long failures) {
		counters.put(key, failures);
		return this;
	}

	public long failuresOf(final String key) {
		return counters.getOrDefault(key, 0L);
	}

	public long blockOf(final String key) {
		return blocks.getOrDefault(key, 0L);
	}

	public List<String> events() {
		return List.copyOf(events);
	}

	@Override
	public CompletionStage<Long> countFailure(final String counterKey, final Duration window) {
		if (down) {
			return failed();
		}
		events.add("count " + counterKey + " " + window.toMinutes() + "m");
		return CompletableFuture.completedFuture(counters.merge(counterKey, 1L, Long::sum));
	}

	@Override
	public CompletionStage<Long> remainingBlockSeconds(final String blockKey) {
		if (down) {
			return failed();
		}
		return CompletableFuture.completedFuture(blocks.getOrDefault(blockKey, 0L));
	}

	@Override
	public CompletionStage<Void> block(final String blockKey, final Duration duration) {
		if (down) {
			return failed();
		}
		events.add("block " + blockKey + " " + duration.toMinutes() + "m");
		blocks.put(blockKey, duration.toSeconds());
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletionStage<Void> forget(final String key) {
		if (down) {
			return failed();
		}
		events.add("forget " + key);
		counters.remove(key);
		return CompletableFuture.completedFuture(null);
	}

	private static <T> CompletionStage<T> failed() {
		return CompletableFuture.failedFuture(new IllegalStateException("Redis caído"));
	}

}
