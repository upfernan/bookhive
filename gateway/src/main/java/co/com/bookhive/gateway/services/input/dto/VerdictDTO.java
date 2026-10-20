package co.com.bookhive.gateway.services.input.dto;

public record VerdictDTO(boolean admitted, boolean unavailable, long retryAfterSeconds) {

	public boolean blocked() {
		return !admitted && !unavailable;
	}

}
