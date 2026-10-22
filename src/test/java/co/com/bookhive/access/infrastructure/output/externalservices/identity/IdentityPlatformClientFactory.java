package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import org.springframework.web.client.RestClient;

public final class IdentityPlatformClientFactory {

	public static final String TOKEN_URL = IdentityPlatformClient.TOKEN_URL;

	private IdentityPlatformClientFactory() {
		super();
	}

	public static IdentityPlatformClient create(final RestClient.Builder builder, final String projectId) {
		return new IdentityPlatformClient(builder, projectId);
	}

}
