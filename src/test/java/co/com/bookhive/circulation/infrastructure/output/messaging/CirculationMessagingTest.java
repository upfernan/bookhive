package co.com.bookhive.circulation.infrastructure.output.messaging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.shared.infrastructure.output.messaging.pubsub.GoogleAccessTokens;
import co.com.bookhive.shared.infrastructure.output.messaging.pubsub.PubSubPublisher;

class CirculationMessagingTest {

	private final List<String> published = new ArrayList<>();
	private boolean failing;

	private final PubSubPublisher publisher = new PubSubPublisher("http://127.0.0.1:1", "p",
			new GoogleAccessTokens("")) {

		@Override
		public void publish(final String topic, final Map<String, String> attributes, final String data) {
			if (failing) {
				throw BookHiveException.createUnexpected(MessageKey.MESSAGING_UNAVAILABLE, "caído");
			}
			published.add(topic + " " + attributes.get("institucion") + " " + attributes.get("tipo")
					+ attributes.getOrDefault("tarea", "") + " " + data);
		}

	};

	private final NotificationRequestEntity notice = new NotificationRequestEntity("PRESTAMO_REGISTRADO",
			UUID.fromString("0190a000-0000-7000-8000-0000000000aa"), UUID.fromString("0190a000-0000-7000-8000-0000000000bb"),
			UUID.fromString("0190a000-0000-7000-8000-0000000000cc"), LocalDate.of(2026, 11, 6));

	@AfterEach
	void clean() {
		TenantContext.clear();
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	@DisplayName("sin transacción publica el aviso de inmediato con la institución y los datos")
	void publishesImmediatelyWithoutTransaction() {
		TenantContext.set("uco");

		new PubSubNotificationRequestAdapter(publisher, "bookhive-avisos").request(notice);

		assertEquals(List.of("bookhive-avisos uco PRESTAMO_REGISTRADO {\"kind\":\"PRESTAMO_REGISTRADO\","
				+ "\"user\":\"0190a000-0000-7000-8000-0000000000aa\",\"book\":\"0190a000-0000-7000-8000-0000000000bb\","
				+ "\"loan\":\"0190a000-0000-7000-8000-0000000000cc\",\"dueDate\":\"2026-11-06\"}"), published);
	}

	@Test
	@DisplayName("dentro de una transacción espera a que se confirme, y si falla la publicación no tumba el préstamo")
	void waitsForCommit() {
		TenantContext.set("uco");
		TransactionSynchronizationManager.initSynchronization();
		final PubSubNotificationRequestAdapter adapter = new PubSubNotificationRequestAdapter(publisher, "bookhive-avisos");

		adapter.request(notice);
		assertTrue(published.isEmpty());

		final List<TransactionSynchronization> pending = TransactionSynchronizationManager.getSynchronizations();
		pending.forEach(TransactionSynchronization::afterCommit);
		assertEquals(1, published.size());

		failing = true;
		assertDoesNotThrow(() -> pending.forEach(TransactionSynchronization::afterCommit));
	}

	@Test
	@DisplayName("nunca pide un aviso sin saber de qué institución es")
	void requiresInstitution() {
		final PubSubNotificationRequestAdapter adapter = new PubSubNotificationRequestAdapter(publisher, "bookhive-avisos");

		assertThrows(BookHiveException.class, () -> adapter.request(notice));
		assertTrue(published.isEmpty());
	}

	@Test
	@DisplayName("la tarea por institución sale al tema de tareas con el módulo, la tarea y la institución")
	void schedulesInstitutionTask() {
		new PubSubInstitutionTaskAdapter(publisher, "bookhive-tareas").schedule("recordar-vencimientos", "unal");

		assertEquals(List.of("bookhive-tareas unal nullrecordar-vencimientos {}"), published);
	}

}
