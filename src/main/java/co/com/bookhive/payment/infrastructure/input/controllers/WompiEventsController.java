package co.com.bookhive.payment.infrastructure.input.controllers;

import java.util.List;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.payment.services.input.dto.request.PaymentEventRequestDTO;
import co.com.bookhive.payment.services.input.dto.response.PaymentEventResponseDTO;
import co.com.bookhive.payment.services.input.ports.RecordPaymentEventInputPort;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RestController
public class WompiEventsController {

	public static final String PATH = "/api/v1/pagos/wompi/eventos";
	static final String TRANSACTION_UPDATED = "transaction.updated";
	static final String CHECKSUM_HEADER = "X-Event-Checksum";

	private static final Logger logger = LoggerFactory.getLogger(WompiEventsController.class);

	private final RecordPaymentEventInputPort recordPaymentEvent;
	private final JsonMapper json = JsonMapper.shared();

	public WompiEventsController(final RecordPaymentEventInputPort recordPaymentEvent) {
		super();
		this.recordPaymentEvent = recordPaymentEvent;
	}

	@PostMapping(PATH)
	public ResponseEntity<Void> receive(
			@RequestBody final String body,
			@RequestHeader(name = CHECKSUM_HEADER, required = false) final String checksumHeader) {

		final JsonNode event;
		try {
			event = json.readTree(TextHelper.getDefaultValue(body));
		} catch (final JacksonException _) {
			logger.warn("Llegó a la dirección de avisos de Wompi algo que no es JSON.");
			return ResponseEntity.badRequest().build();
		}
		final String type = event.path("event").asString("");
		if (!TRANSACTION_UPDATED.equals(type)) {
			logger.info("Se ignoró un aviso de Wompi de tipo {}.", type);
			return ResponseEntity.ok().build();
		}

		final JsonNode data = event.path("data");
		final JsonNode transaction = data.path("transaction");
		final JsonNode signature = event.path("signature");
		final String checksum = TextHelper.isDefaultValue(checksumHeader)
				? signature.path("checksum").asString("")
				: checksumHeader;

		final String transactionId = transaction.path("id").asString("");
		final String status = transaction.path("status").asString("");
		final PaymentEventResponseDTO response = recordPaymentEvent.execute(new PaymentEventRequestDTO(
				transaction.path("reference").asString(""),
				transactionId,
				status,
				transaction.path("amount_in_cents").asLong(0L),
				signedValuesOf(signature.path("properties"), data),
				event.path("timestamp").asLong(0L),
				checksum));

		final String outcome = response.applied() ? "registrado" : "no corresponde a un pago de BookHive";
		logger.info("Aviso de Wompi de la transacción {} en estado {}: {}.", transactionId, status, outcome);
		return ResponseEntity.ok().build();
	}

	private static List<String> signedValuesOf(final JsonNode properties, final JsonNode data) {
		return StreamSupport.stream(properties.spliterator(), false)
				.map(property -> valueAt(data, property.asString("")))
				.toList();
	}

	private static String valueAt(final JsonNode data, final String path) {
		JsonNode current = data;
		for (final String step : TextHelper.getDefaultValue(path).split("\\.")) {
			current = current.path(step);
		}
		return current.isValueNode() ? current.asString("") : "";
	}

}
