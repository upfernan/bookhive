package co.com.bookhive.payment.infrastructure.output.externalservices.paymentgateway;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.resilience.CircuitBreaker;
import co.com.bookhive.crosscutting.resilience.CircuitOpenException;
import co.com.bookhive.payment.services.output.ports.PaymentGatewayPort;
import co.com.bookhive.payment.services.output.ports.entities.CheckoutEntity;
import co.com.bookhive.payment.services.output.ports.entities.CheckoutRequestEntity;
import co.com.bookhive.payment.services.output.ports.entities.GatewayTransactionEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class WompiPaymentGatewayAdapter implements PaymentGatewayPort {

	static final String SERVICE = "Wompi";
	static final int FAILURES_BEFORE_CUT = 3;
	static final Duration CUT_DURATION = Duration.ofSeconds(30);

	private static final Logger logger = LoggerFactory.getLogger(WompiPaymentGatewayAdapter.class);
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
	private static final DateTimeFormatter EXPIRATION =
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).withZone(ZoneOffset.UTC);
	private static final DateTimeFormatter DAY = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);
	private static final String BEARER = "Bearer ";
	private static final int NOT_FOUND = 404;

	private final String apiUrl;
	private final String checkoutUrl;
	private final String publicKey;
	private final String privateKey;
	private final String eventsSecret;
	private final String integritySecret;
	private final String redirectUrl;
	private final RestClient client;
	private final CircuitBreaker breaker;
	private final JsonMapper json = JsonMapper.shared();

	public WompiPaymentGatewayAdapter(
			@Value("${bookhive.payments.wompi.api-url:https://sandbox.wompi.co/v1}") final String apiUrl,
			@Value("${bookhive.payments.wompi.checkout-url:https://checkout.wompi.co/p/}") final String checkoutUrl,
			@Value("${bookhive.payments.wompi.public-key:}") final String publicKey,
			@Value("${bookhive.payments.wompi.private-key:}") final String privateKey,
			@Value("${bookhive.payments.wompi.events-secret:}") final String eventsSecret,
			@Value("${bookhive.payments.wompi.integrity-secret:}") final String integritySecret,
			@Value("${bookhive.payments.redirect-url:}") final String redirectUrl) {

		super();
		this.apiUrl = TextHelper.getDefaultValue(apiUrl).trim().replaceAll("/+$", "");
		this.checkoutUrl = TextHelper.getDefaultValue(checkoutUrl).trim();
		this.publicKey = TextHelper.getDefaultValue(publicKey).trim();
		this.privateKey = TextHelper.getDefaultValue(privateKey).trim();
		this.eventsSecret = TextHelper.getDefaultValue(eventsSecret).trim();
		this.integritySecret = TextHelper.getDefaultValue(integritySecret).trim();
		this.redirectUrl = TextHelper.getDefaultValue(redirectUrl).trim();

		final JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
				HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.client = RestClient.builder().requestFactory(requestFactory).build();
		this.breaker = new CircuitBreaker(SERVICE, FAILURES_BEFORE_CUT, CUT_DURATION, Clock.systemUTC(), logger::error);
	}

	@Override
	public CheckoutEntity checkoutFor(final CheckoutRequestEntity request) {
		requireConfigured(publicKey, "bookhive.payments.wompi.public-key");
		requireConfigured(integritySecret, "bookhive.payments.wompi.integrity-secret");

		final CheckoutRequestEntity safeRequest =
				ObjectHelper.getDefaultValue(request, new CheckoutRequestEntity(null, 0L, null, null));
		final String expiration = EXPIRATION.format(safeRequest.expiresAt());
		final String signature = sha256(safeRequest.reference() + safeRequest.amountInCents()
				+ safeRequest.currency() + expiration + integritySecret);

		return new CheckoutEntity(checkoutUrl, publicKey, signature, expiration, redirectUrl);
	}

	@Override
	public Optional<GatewayTransactionEntity> findTransaction(final String transaction) {
		requireConfigured(publicKey, "bookhive.payments.wompi.public-key");
		final String id = TextHelper.getDefaultValue(transaction).trim();

		final WompiResponse response = guarded("consultar la transacción " + id, () -> client.get()
				.uri(apiUrl + "/transactions/{id}", id)
				.header(HttpHeaders.AUTHORIZATION, BEARER + publicKey)
				.accept(MediaType.APPLICATION_JSON)
				.exchange((sent, received) -> WompiResponse.of(
						received.getStatusCode().value(), received.getBody().readAllBytes())));

		if (response.status() == NOT_FOUND) {
			return Optional.empty();
		}
		return Optional.of(transactionOf(data(response.body())));
	}

	@Override
	public List<GatewayTransactionEntity> findTransactionsOf(final String reference, final Instant createdAt) {
		requireConfigured(privateKey, "bookhive.payments.wompi.private-key");
		final String safeReference = TextHelper.getDefaultValue(reference).trim();
		final Instant created = ObjectHelper.getDefaultValue(createdAt, Instant.EPOCH);

		final WompiResponse response = guarded("buscar las transacciones de " + safeReference, () -> client.get()
				.uri(apiUrl + "/transactions?reference={reference}&from_date={from}&until_date={until}",
						safeReference,
						DAY.format(created.minus(Duration.ofDays(1))),
						DAY.format(created.plus(Duration.ofDays(4))))
				.header(HttpHeaders.AUTHORIZATION, BEARER + privateKey)
				.accept(MediaType.APPLICATION_JSON)
				.exchange((sent, received) -> WompiResponse.of(
						received.getStatusCode().value(), received.getBody().readAllBytes())));

		if (response.status() == NOT_FOUND) {
			return List.of();
		}
		return StreamSupport.stream(data(response.body()).spliterator(), false)
				.map(WompiPaymentGatewayAdapter::transactionOf)
				.toList();
	}

	@Override
	public boolean isAuthentic(final List<String> signedValues, final long timestamp, final String checksum) {
		if (TextHelper.isDefaultValue(eventsSecret)) {
			logger.error("Llegó un aviso de Wompi y no hay secreto de eventos configurado"
					+ " (bookhive.payments.wompi.events-secret, ADR-043).");
			return false;
		}
		final String received = TextHelper.getDefaultValue(checksum).trim().toLowerCase(Locale.ROOT);
		if (TextHelper.isDefaultValue(received) || timestamp <= 0) {
			return false;
		}
		final String expected = sha256(String.join("", ObjectHelper.getDefaultValue(signedValues, List.<String>of()))
				+ timestamp + eventsSecret);
		return MessageDigest.isEqual(
				expected.getBytes(StandardCharsets.UTF_8), received.getBytes(StandardCharsets.UTF_8));
	}

	private WompiResponse guarded(final String operation, final Supplier<WompiResponse> call) {
		try {
			return breaker.call(() -> {
				final WompiResponse response = call.get();
				if (response.status() != NOT_FOUND && (response.status() < 200 || response.status() >= 300)) {
					throw new IllegalStateException("Wompi respondió " + response.status() + " al " + operation + ".");
				}
				return response;
			});
		} catch (final CircuitOpenException exception) {
			throw unavailable(exception, "No se llamó a Wompi para " + operation + ": el corte temporal está abierto.");
		} catch (final RestClientException | IllegalStateException exception) {
			throw unavailable(exception, "Wompi no respondió bien al " + operation + ": " + exception.getMessage());
		}
	}

	private JsonNode data(final String body) {
		try {
			return json.readTree(body).path("data");
		} catch (final JacksonException exception) {
			throw unavailable(exception, "Wompi respondió algo que no es JSON.");
		}
	}

	private static GatewayTransactionEntity transactionOf(final JsonNode transaction) {
		return new GatewayTransactionEntity(
				transaction.path("id").asString(""),
				transaction.path("reference").asString(""),
				transaction.path("status").asString(""),
				transaction.path("amount_in_cents").asLong(0L));
	}

	private static void requireConfigured(final String value, final String property) {
		if (TextHelper.isDefaultValue(value)) {
			throw unavailable(new IllegalStateException(property),
					"Falta " + property + ": el pago en línea no está configurado (ADR-043).");
		}
	}

	private static BookHiveException unavailable(final Throwable cause, final String technicalMessage) {
		return BookHiveException.createUnavailable(cause, MessageKey.PAYMENT_GATEWAY_UNAVAILABLE, technicalMessage);
	}

	static String sha256(final String text) {
		try {
			return HexFormat.of().formatHex(
					MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
		} catch (final NoSuchAlgorithmException exception) {
			throw BookHiveException.createUnexpected(
					exception, MessageKey.PAYMENT_UNEXPECTED, "La máquina no tiene SHA-256.");
		}
	}

	private record WompiResponse(int status, String body) {

		static WompiResponse of(final int status, final byte[] body) {
			return new WompiResponse(status, new String(body, StandardCharsets.UTF_8));
		}

	}

}
