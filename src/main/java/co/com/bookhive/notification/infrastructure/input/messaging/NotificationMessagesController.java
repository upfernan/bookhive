package co.com.bookhive.notification.infrastructure.input.messaging;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.notification.services.input.dto.request.SendNotificationRequestDTO;
import co.com.bookhive.notification.services.input.dto.response.SendNotificationResponseDTO;
import co.com.bookhive.notification.services.input.ports.SendNotificationInputPort;
import co.com.bookhive.shared.infrastructure.input.messaging.PubSubPushMessage;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/v1/avisos")
public class NotificationMessagesController {

	private static final Logger logger = LoggerFactory.getLogger(NotificationMessagesController.class);
	private static final Pattern INSTITUTION = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

	private final SendNotificationInputPort sendNotification;
	private final JsonMapper json = JsonMapper.shared();

	public NotificationMessagesController(final SendNotificationInputPort sendNotification) {
		super();
		this.sendNotification = sendNotification;
	}

	@PostMapping("/correo")
	public ResponseEntity<Void> receive(@RequestBody final PubSubPushMessage push) {
		final String institution = push.attribute("institucion");
		if (!INSTITUTION.matcher(institution).matches()) {
			logger.warn("Se descartó el aviso {}: no trae una institución válida.", push.messageId());
			return ResponseEntity.noContent().build();
		}

		final SendNotificationRequestDTO request = requestOf(push.text());
		TenantContext.set(institution);
		try {
			final SendNotificationResponseDTO response = sendNotification.execute(request);
			logger.info("Aviso {} de {} atendido: {}.", request.kind(), institution, response.status());
		} catch (final BookHiveException exception) {
			if (!BookHiveException.Kind.MISSING_DATA.equals(exception.getKind())) {
				throw exception;
			}
			logger.warn("Se descartó el aviso {}: {}", push.messageId(), exception.getTechnicalMessage());
		} finally {
			TenantContext.clear();
		}
		return ResponseEntity.noContent().build();
	}

	private SendNotificationRequestDTO requestOf(final String text) {
		try {
			final JsonNode data = json.readTree(text);
			return new SendNotificationRequestDTO(
					data.path("kind").asString(""),
					uuidOf(data.path("user").asString("")),
					uuidOf(data.path("book").asString("")),
					uuidOf(data.path("loan").asString("")),
					dateOf(data.path("dueDate").asString("")));
		} catch (final JacksonException _) {
			return new SendNotificationRequestDTO("", null, null, null, null);
		}
	}

	private static UUID uuidOf(final String value) {
		try {
			return UUID.fromString(value);
		} catch (final IllegalArgumentException _) {
			return null;
		}
	}

	private static LocalDate dateOf(final String value) {
		try {
			return LocalDate.parse(value);
		} catch (final DateTimeParseException _) {
			return null;
		}
	}

}
