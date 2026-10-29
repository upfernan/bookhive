package co.com.bookhive.circulation.infrastructure.output.messaging;

import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import co.com.bookhive.circulation.services.output.ports.NotificationRequestPort;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.shared.infrastructure.output.messaging.pubsub.PubSubPublisher;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class PubSubNotificationRequestAdapter implements NotificationRequestPort {

	private static final Logger logger = LoggerFactory.getLogger(PubSubNotificationRequestAdapter.class);
	private static final Pattern INSTITUTION = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

	private final PubSubPublisher publisher;
	private final String topic;
	private final JsonMapper json = JsonMapper.shared();

	public PubSubNotificationRequestAdapter(
			final PubSubPublisher publisher,
			@Value("${bookhive.messaging.notices-topic:bookhive-avisos}") final String topic) {

		super();
		this.publisher = publisher;
		this.topic = topic;
	}

	@Override
	public void request(final NotificationRequestEntity notification) {
		final String institution = TenantContext.get();
		if (TenantContext.SCHEMA_WITHOUT_TENANT.equals(institution) || !INSTITUTION.matcher(institution).matches()) {
			throw BookHiveException.createUnexpected(
					MessageKey.MESSAGING_UNAVAILABLE,
					"Se intentó pedir un aviso sin una institución resuelta.");
		}

		final ObjectNode data = json.createObjectNode();
		data.put("kind", notification.kind());
		data.put("user", notification.user().toString());
		data.put("book", notification.book().toString());
		data.put("loan", notification.loan().toString());
		data.put("dueDate", notification.dueDate().toString());
		final String message = json.writeValueAsString(data);
		final Map<String, String> attributes = Map.of("tipo", notification.kind(), "institucion", institution);

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					publishAfterCommit(attributes, message, notification);
				}
			});
			return;
		}
		publisher.publish(topic, attributes, message);
	}

	private void publishAfterCommit(
			final Map<String, String> attributes,
			final String message,
			final NotificationRequestEntity notification) {

		try {
			publisher.publish(topic, attributes, message);
		} catch (final RuntimeException exception) {
			logger.warn("La operación quedó guardada, pero no se pudo dejar en la cola el aviso {} del préstamo {}.",
					notification.kind(), notification.loan(), exception);
		}
	}

}
