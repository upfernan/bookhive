package co.com.bookhive.access.services.input.dto.request;

import java.util.UUID;

public record ChangePersonRequestDTO(UUID person, String schema, String action) {

	public static final String DEACTIVATE = "DEACTIVATE";
	public static final String REACTIVATE = "REACTIVATE";
	public static final String ANONYMIZE = "ANONYMIZE";

}
