package co.com.bookhive.circulation.infrastructure.output.messaging;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import co.com.bookhive.circulation.services.output.ports.InstitutionTaskPort;
import co.com.bookhive.shared.infrastructure.output.messaging.pubsub.PubSubPublisher;

@Component
public class PubSubInstitutionTaskAdapter implements InstitutionTaskPort {

	static final String MODULE = "prestamos";

	private final PubSubPublisher publisher;
	private final String topic;

	public PubSubInstitutionTaskAdapter(
			final PubSubPublisher publisher,
			@Value("${bookhive.messaging.tasks-topic:bookhive-tareas}") final String topic) {

		super();
		this.publisher = publisher;
		this.topic = topic;
	}

	@Override
	public void schedule(final String task, final String institution) {
		publisher.publish(topic, Map.of("modulo", MODULE, "tarea", task, "institucion", institution), "{}");
	}

}
