package co.com.bookhive.access.infrastructure.output.externalservices.identity;

public record IdentityPlatformAccount(long validSince, boolean disabled) {

	public boolean acceptsSessionStartedAt(final long authTime) {
		return !disabled && authTime >= validSince;
	}

}
