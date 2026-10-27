package co.com.bookhive.circulation.services.input.dto.request;

import java.util.UUID;

public record UserLifecycleRequestDTO(UUID user, String change) {

	public static final String DEACTIVATE = "DEACTIVATE";
	public static final String REACTIVATE = "REACTIVATE";
	public static final String PURGE = "PURGE";

}
