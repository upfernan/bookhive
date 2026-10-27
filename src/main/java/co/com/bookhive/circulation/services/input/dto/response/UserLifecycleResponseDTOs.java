package co.com.bookhive.circulation.services.input.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class UserLifecycleResponseDTOs {

	private UserLifecycleResponseDTOs() {
		super();
	}

	public record Account(UUID user, String status, Instant deactivatedAt) {
	}

	public record Retention(int months) {
	}

	public record PurgeRun(String institution, int purged, int skipped) {
	}

	public record PurgeRuns(List<PurgeRun> institutions) {
	}

}
