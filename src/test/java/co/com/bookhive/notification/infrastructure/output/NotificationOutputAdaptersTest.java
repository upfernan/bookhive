package co.com.bookhive.notification.infrastructure.output;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.catalog.services.input.dto.response.BookTitleResponseDTO;
import co.com.bookhive.circulation.services.input.dto.response.UserContactResponseDTO;
import co.com.bookhive.notification.infrastructure.output.externalservices.catalog.CatalogBookTitleAdapter;
import co.com.bookhive.notification.infrastructure.output.externalservices.circulation.CirculationRecipientAdapter;
import co.com.bookhive.notification.infrastructure.output.externalservices.messagecatalog.MessageCatalogTemplateAdapter;
import co.com.bookhive.notification.infrastructure.output.repositories.structured.adapters.NotificationRepositoryAdapter;
import co.com.bookhive.notification.infrastructure.output.repositories.structured.jpa.NotificationJpaRepository;
import co.com.bookhive.notification.infrastructure.output.repositories.structured.jpa.entities.NotificationJpaEntity;
import co.com.bookhive.notification.services.output.ports.entities.EmailEntity;
import co.com.bookhive.notification.services.output.ports.entities.RecipientEntity;
import co.com.bookhive.notification.services.output.ports.repositories.criteria.NotificationCriteria;
import co.com.bookhive.notification.services.output.ports.repositories.entities.NotificationEntity;
import co.com.bookhive.shared.services.input.dto.response.ResolveMessageResponseDTO;

class NotificationOutputAdaptersTest {

	private static final UUID USER = UUID.randomUUID();
	private static final UUID BOOK = UUID.randomUUID();

	@Test
	@DisplayName("pide el contacto al módulo de préstamos, el título al catálogo y el texto al catálogo de mensajes")
	void asksOtherModules() {
		final CirculationRecipientAdapter recipients = new CirculationRecipientAdapter(
				request -> new UserContactResponseDTO(request.user(), "Ana Pérez", "ana@uco.edu.co"));
		final CatalogBookTitleAdapter titles = new CatalogBookTitleAdapter(
				request -> new BookTitleResponseDTO(request.book(), "Clean Code"));
		final MessageCatalogTemplateAdapter templates = new MessageCatalogTemplateAdapter(
				request -> new ResolveMessageResponseDTO("Texto de " + request.messageKey()));

		assertEquals(new RecipientEntity("Ana Pérez", "ana@uco.edu.co"), recipients.find(USER));
		assertEquals("Clean Code", titles.titleOf(BOOK));
		assertEquals("Texto de email.loan-registered.subject", templates.textOf("email.loan-registered.subject"));
	}

	@Test
	@DisplayName("guarda la constancia del aviso y la busca por su llave, sin consultar si no hay llave")
	void storesAndFindsNotifications() {
		final List<NotificationJpaEntity> saved = new ArrayList<>();
		final List<String> searched = new ArrayList<>();
		final NotificationJpaRepository repository = (NotificationJpaRepository) Proxy.newProxyInstance(
				NotificationJpaRepository.class.getClassLoader(),
				new Class<?>[] { NotificationJpaRepository.class },
				(proxy, method, arguments) -> switch (method.getName()) {
					case "saveAndFlush" -> {
						saved.add((NotificationJpaEntity) arguments[0]);
						yield arguments[0];
					}
					case "findByIdempotencyKey" -> {
						searched.add((String) arguments[0]);
						yield List.copyOf(saved);
					}
					default -> throw new UnsupportedOperationException(method.getName());
				});
		final NotificationRepositoryAdapter adapter = new NotificationRepositoryAdapter(repository);
		final NotificationEntity notice = new NotificationEntity(UUID.randomUUID(), "PRESTAMO_REGISTRADO:x:2026-11-06",
				"PRESTAMO_REGISTRADO", USER, NotificationEntity.CHANNEL_EMAIL, "ENVIADO", "<id>");

		adapter.create(notice);

		assertEquals(List.of(notice), adapter.findByCriteria(NotificationCriteria.byKey(notice.idempotencyKey())));
		assertEquals(List.of(notice.idempotencyKey()), searched);
		assertTrue(adapter.findByCriteria(null).isEmpty());
		assertTrue(adapter.findByCriteria(NotificationCriteria.byKey(" ")).isEmpty());
		assertEquals(1, searched.size());
	}

	@Test
	@DisplayName("las entidades del aviso nunca quedan con datos nulos")
	void entitiesHaveSafeDefaults() {
		final NotificationJpaEntity jpa = new NotificationJpaEntity();
		assertEquals(new UUID(0, 0), jpa.getId());
		assertEquals("", jpa.getProviderMessageId());
		assertEquals(new NotificationEntity(null, "", "", null, "", "", ""),
				new NotificationEntity(null, null, null, null, null, null, null));
		assertEquals(new EmailEntity("", "", "", ""), new EmailEntity(null, null, null, null));
		assertEquals(new RecipientEntity("", ""), new RecipientEntity(null, null));
		assertEquals("", new NotificationCriteria(null).idempotencyKey());
	}

}
