package co.com.bookhive.payment.infrastructure.output.externalservices.paymentgateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.payment.services.output.ports.entities.CheckoutEntity;
import co.com.bookhive.payment.services.output.ports.entities.CheckoutRequestEntity;
import co.com.bookhive.payment.services.output.ports.entities.GatewayTransactionEntity;

class WompiPaymentGatewayAdapterTest {

	private static final String TRANSACTION = "{\"data\":{\"id\":\"12-1761480000-4920\",\"reference\":\"uco-abc\","
			+ "\"status\":\"APPROVED\",\"amount_in_cents\":300000}}";
	private static final String TRANSACTIONS = "{\"data\":[{\"id\":\"12-1\",\"reference\":\"uco-abc\","
			+ "\"status\":\"DECLINED\",\"amount_in_cents\":300000},{\"id\":\"12-2\",\"reference\":\"uco-abc\","
			+ "\"status\":\"APPROVED\",\"amount_in_cents\":300000}]}";

	private HttpServer server;
	private final List<String> requests = new ArrayList<>();
	private int status = 200;
	private String body = TRANSACTION;

	@BeforeEach
	void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", this::answer);
		server.start();
	}

	@AfterEach
	void stop() {
		server.stop(0);
	}

	@Test
	@DisplayName("firma el pago con referencia, valor, moneda, vencimiento y secreto de integridad, en ese orden")
	void signsCheckout() {
		final CheckoutEntity checkout = adapter().checkoutFor(new CheckoutRequestEntity(
				"uco-abc", 300000L, "COP", Instant.parse("2026-10-26T12:30:00Z")));

		assertEquals("2d918aff9a7bcb23a7b7738c66de2c1a149d3111d764f3e2e22e0058ad551c03", checkout.signature());
		assertEquals("2026-10-26T12:30:00.000Z", checkout.expirationTime());
		assertEquals("pub_test_publica", checkout.publicKey());
		assertEquals("https://checkout.wompi.co/p/", checkout.checkoutUrl());
		assertEquals("https://bookhive.test/pagos/resultado", checkout.redirectUrl());
		assertTrue(requests.isEmpty());
	}

	@Test
	@DisplayName("consulta una transacción con la llave pública")
	void findsTransactionWithPublicKey() {
		final GatewayTransactionEntity transaction = adapter().findTransaction("12-1761480000-4920").orElseThrow();

		assertEquals("12-1761480000-4920", transaction.id());
		assertEquals("uco-abc", transaction.reference());
		assertEquals("APPROVED", transaction.status());
		assertEquals(300000L, transaction.amountInCents());
		assertEquals(List.of("GET /v1/transactions/12-1761480000-4920 Bearer pub_test_publica"), requests);
	}

	@Test
	@DisplayName("una transacción que la pasarela no conoce no existe")
	void unknownTransactionIsEmpty() {
		status = 404;
		body = "{\"error\":{}}";

		assertTrue(adapter().findTransaction("12-9").isEmpty());
	}

	@Test
	@DisplayName("busca las transacciones de una referencia con la llave privada")
	void findsTransactionsOfReferenceWithPrivateKey() {
		body = TRANSACTIONS;

		final List<GatewayTransactionEntity> transactions =
				adapter().findTransactionsOf("uco-abc", Instant.parse("2026-10-26T12:00:00Z"));

		assertEquals(2, transactions.size());
		assertEquals("APPROVED", transactions.get(1).status());
		assertEquals(List.of("GET /v1/transactions?reference=uco-abc&from_date=2026-10-25&until_date=2026-10-30"
				+ " Bearer prv_test_privada"), requests);
	}

	@Test
	@DisplayName("si Wompi falla seguido deja de llamarlo por un rato y responde que no está disponible")
	void cutsCallsAfterConsecutiveFailures() {
		status = 500;
		final WompiPaymentGatewayAdapter adapter = adapter();

		for (int attempt = 0; attempt < WompiPaymentGatewayAdapter.FAILURES_BEFORE_CUT + 1; attempt++) {
			final BookHiveException exception =
					assertThrows(BookHiveException.class, () -> adapter.findTransaction("12-1"));
			assertEquals(BookHiveException.Kind.UNAVAILABLE, exception.getKind());
		}

		assertEquals(WompiPaymentGatewayAdapter.FAILURES_BEFORE_CUT, requests.size());
	}

	@Test
	@DisplayName("una respuesta que no es JSON se trata como pasarela no disponible")
	void invalidJsonIsUnavailable() {
		body = "no es json";
		final WompiPaymentGatewayAdapter adapter = adapter();

		assertEquals(BookHiveException.Kind.UNAVAILABLE,
				assertThrows(BookHiveException.class, () -> adapter.findTransaction("12-1")).getKind());
	}

	@Test
	@DisplayName("un aviso es auténtico solo si su firma coincide con el secreto de eventos")
	void verifiesEventChecksum() {
		final List<String> values = List.of("12-1761480000-4920", "APPROVED", "300000");
		final WompiPaymentGatewayAdapter adapter = adapter();

		assertTrue(adapter.isAuthentic(values, 1761480000L,
				"573EE8659475EA016BD3BA247670B2FEC1A41666AB0F2BE8B957B044A243F582"));
		assertFalse(adapter.isAuthentic(values, 1761480001L,
				"573ee8659475ea016bd3ba247670b2fec1a41666ab0f2be8b957b044a243f582"));
		assertFalse(adapter.isAuthentic(values, 1761480000L, ""));
		assertFalse(adapter.isAuthentic(values, 0L, "573ee8"));
	}

	@Test
	@DisplayName("sin llaves configuradas el pago en línea no está disponible y ningún aviso es auténtico")
	void withoutKeysNothingWorks() {
		final WompiPaymentGatewayAdapter adapter =
				new WompiPaymentGatewayAdapter(url(), "https://checkout.wompi.co/p/", "", "", "", "", "");
		final CheckoutRequestEntity request = new CheckoutRequestEntity("uco-abc", 1L, "COP", Instant.now());
		final Instant now = Instant.now();

		assertEquals(BookHiveException.Kind.UNAVAILABLE,
				assertThrows(BookHiveException.class, () -> adapter.checkoutFor(request)).getKind());
		assertEquals(BookHiveException.Kind.UNAVAILABLE,
				assertThrows(BookHiveException.class, () -> adapter.findTransaction("12-1")).getKind());
		assertEquals(BookHiveException.Kind.UNAVAILABLE,
				assertThrows(BookHiveException.class, () -> adapter.findTransactionsOf("uco-abc", now)).getKind());
		assertFalse(adapter.isAuthentic(List.of("a"), 1L, "x"));
		assertTrue(requests.isEmpty());
	}

	private WompiPaymentGatewayAdapter adapter() {
		return new WompiPaymentGatewayAdapter(
				url() + "/",
				"https://checkout.wompi.co/p/",
				"pub_test_publica",
				"prv_test_privada",
				"test_events_secreto",
				"test_integrity_secreto",
				"https://bookhive.test/pagos/resultado");
	}

	private String url() {
		return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
	}

	private void answer(final HttpExchange exchange) throws IOException {
		requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI() + " "
				+ exchange.getRequestHeaders().getFirst("Authorization"));
		final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, bytes.length);
		exchange.getResponseBody().write(bytes);
		exchange.close();
	}

}
