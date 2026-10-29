package co.com.bookhive.access.infrastructure.output.externalservices.identity;

import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

final class GoogleHttp {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

	private GoogleHttp() {
		super();
	}

	static RestClient.Builder builder() {

		final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);

		return RestClient.builder().requestFactory(requestFactory);
	}

}
