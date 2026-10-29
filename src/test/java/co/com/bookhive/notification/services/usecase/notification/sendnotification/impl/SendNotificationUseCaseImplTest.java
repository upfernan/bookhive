package co.com.bookhive.notification.services.usecase.notification.sendnotification.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.notification.domain.notification.sendnotification.NotificationOutcomeDomain;
import co.com.bookhive.notification.domain.notification.sendnotification.SendNotificationDomain;
import co.com.bookhive.notification.services.input.adapters.SendNotificationInputAdapter;
import co.com.bookhive.notification.services.input.dto.request.SendNotificationRequestDTO;
import co.com.bookhive.notification.services.output.ports.BookTitlePort;
import co.com.bookhive.notification.services.output.ports.EmailPort;
import co.com.bookhive.notification.services.output.ports.MessageTemplatePort;
import co.com.bookhive.notification.services.output.ports.RecipientPort;
import co.com.bookhive.notification.services.output.ports.entities.EmailEntity;
import co.com.bookhive.notification.services.output.ports.entities.RecipientEntity;
import co.com.bookhive.notification.services.output.ports.repositories.NotificationRepository;
import co.com.bookhive.notification.services.output.ports.repositories.criteria.NotificationCriteria;
import co.com.bookhive.notification.services.output.ports.repositories.entities.NotificationEntity;

class SendNotificationUseCaseImplTest {

	private static final UUID USER = UUID.randomUUID();
	private static final UUID BOOK = UUID.randomUUID();
	private static final UUID LOAN = UUID.randomUUID();
	private static final LocalDate DUE = LocalDate.of(2026, 11, 6);

	private final List<NotificationEntity> history = new ArrayList<>();
	private final List<EmailEntity> sent = new ArrayList<>();
	private RecipientEntity recipient = new RecipientEntity("Ana Pérez", "ana@uco.edu.co");

	private final NotificationRepository repository = new NotificationRepository() {

		@Override
		public List<NotificationEntity> findByCriteria(final NotificationCriteria criteria) {
			return history.stream().filter(notice -> notice.idempotencyKey().equals(criteria.idempotencyKey())).toList();
		}

		@Override
		public void create(final NotificationEntity entity) {
			history.add(entity);
		}

	};

	private final RecipientPort recipients = user -> recipient;
	private final BookTitlePort titles = book -> "Clean Code";
	private final MessageTemplatePort templates = key -> Map.of(
			MessageKey.EMAIL_LOAN_REGISTERED_SUBJECT, "Préstamo de «{titulo}»",
			MessageKey.EMAIL_LOAN_REGISTERED_BODY, "Hola {nombre}, devuélvelo el {fecha}.").getOrDefault(key, key);
	private final EmailPort email = message -> {
		sent.add(message);
		return "<id-brevo@smtp-relay>";
	};

	@Test
	@DisplayName("envía el correo con el texto del catálogo y deja constancia del envío")
	void sendsAndRecords() {
		final NotificationOutcomeDomain outcome = useCase().execute(notice());

		assertEquals(NotificationOutcomeDomain.SENT, outcome.status());
		assertEquals(List.of(new EmailEntity("ana@uco.edu.co", "Ana Pérez", "Préstamo de «Clean Code»",
				"Hola Ana Pérez, devuélvelo el 06/11/2026.")), sent);
		final NotificationEntity saved = history.get(0);
		assertEquals(notice().idempotencyKey(), saved.idempotencyKey());
		assertEquals(NotificationEntity.CHANNEL_EMAIL, saved.channel());
		assertEquals("<id-brevo@smtp-relay>", saved.providerMessageId());
		assertEquals(USER, saved.user());
	}

	@Test
	@DisplayName("si Pub/Sub entrega el mismo aviso dos veces, el correo sale una sola vez")
	void doesNotSendTwice() {
		final SendNotificationUseCaseImpl useCase = useCase();

		useCase.execute(notice());
		final NotificationOutcomeDomain second = useCase.execute(notice());

		assertEquals(NotificationOutcomeDomain.ALREADY_SENT, second.status());
		assertEquals(1, sent.size());
		assertEquals(1, history.size());
	}

	@Test
	@DisplayName("si el usuario no tiene correo no envía nada, pero deja constancia")
	void recordsWhenThereIsNoEmail() {
		recipient = new RecipientEntity("", "");

		final NotificationOutcomeDomain outcome = useCase().execute(notice());

		assertEquals(NotificationOutcomeDomain.WITHOUT_EMAIL, outcome.status());
		assertTrue(sent.isEmpty());
		assertEquals(NotificationOutcomeDomain.WITHOUT_EMAIL, history.get(0).status());
	}

	@Test
	@DisplayName("si Brevo falla no deja constancia, para que Pub/Sub lo vuelva a intentar")
	void doesNotRecordWhenEmailFails() {
		final SendNotificationUseCaseImpl failing = new SendNotificationUseCaseImpl(repository, recipients, titles,
				templates, message -> {
					throw BookHiveException.createUnexpected(MessageKey.EMAIL_UNAVAILABLE, "Brevo caído.");
				});
		final SendNotificationDomain notice = notice();

		assertThrows(BookHiveException.class, () -> failing.execute(notice));
		assertTrue(history.isEmpty());
	}

	@Test
	@DisplayName("rechaza avisos incompletos y no se crea sin sus puertos")
	void validatesNoticeAndDependencies() {
		final SendNotificationUseCaseImpl useCase = useCase();

		assertThrows(BookHiveException.class, () -> useCase.execute(null));
		assertThrows(BookHiveException.class, () -> new SendNotificationUseCaseImpl(null, recipients, titles, templates, email));
		assertThrows(BookHiveException.class, () -> new SendNotificationUseCaseImpl(repository, null, titles, templates, email));
		assertThrows(BookHiveException.class, () -> new SendNotificationUseCaseImpl(repository, recipients, null, templates, email));
		assertThrows(BookHiveException.class, () -> new SendNotificationUseCaseImpl(repository, recipients, titles, null, email));
		assertThrows(BookHiveException.class, () -> new SendNotificationUseCaseImpl(repository, recipients, titles, templates, null));
	}

	@Test
	@DisplayName("el puerto de entrada convierte la petición en dominio y devuelve el resultado")
	void inputAdapterMapsRequest() {
		final SendNotificationInputAdapter adapter = new SendNotificationInputAdapter(useCase());

		assertEquals(NotificationOutcomeDomain.SENT, adapter.execute(new SendNotificationRequestDTO(
				SendNotificationDomain.KIND_LOAN_REGISTERED, USER, BOOK, LOAN, DUE)).status());
		assertThrows(BookHiveException.class, () -> adapter.execute(null));
	}

	private SendNotificationUseCaseImpl useCase() {
		return new SendNotificationUseCaseImpl(repository, recipients, titles, templates, email);
	}

	private static SendNotificationDomain notice() {
		return new SendNotificationDomain(SendNotificationDomain.KIND_LOAN_REGISTERED, USER, BOOK, LOAN, DUE);
	}

}
