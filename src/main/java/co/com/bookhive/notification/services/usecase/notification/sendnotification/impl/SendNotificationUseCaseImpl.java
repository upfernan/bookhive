package co.com.bookhive.notification.services.usecase.notification.sendnotification.impl;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.notification.domain.notification.sendnotification.NotificationOutcomeDomain;
import co.com.bookhive.notification.domain.notification.sendnotification.SendNotificationDomain;
import co.com.bookhive.notification.services.output.ports.BookTitlePort;
import co.com.bookhive.notification.services.output.ports.EmailPort;
import co.com.bookhive.notification.services.output.ports.MessageTemplatePort;
import co.com.bookhive.notification.services.output.ports.RecipientPort;
import co.com.bookhive.notification.services.output.ports.entities.EmailEntity;
import co.com.bookhive.notification.services.output.ports.entities.RecipientEntity;
import co.com.bookhive.notification.services.output.ports.repositories.NotificationRepository;
import co.com.bookhive.notification.services.output.ports.repositories.criteria.NotificationCriteria;
import co.com.bookhive.notification.services.output.ports.repositories.entities.NotificationEntity;
import co.com.bookhive.notification.services.usecase.notification.sendnotification.SendNotificationUseCase;

public final class SendNotificationUseCaseImpl implements SendNotificationUseCase {

	private final NotificationRepository notificationRepository;
	private final RecipientPort recipients;
	private final BookTitlePort bookTitles;
	private final MessageTemplatePort templates;
	private final EmailPort email;

	public SendNotificationUseCaseImpl(
			final NotificationRepository notificationRepository,
			final RecipientPort recipients,
			final BookTitlePort bookTitles,
			final MessageTemplatePort templates,
			final EmailPort email) {

		super();
		this.notificationRepository = requireDependency(notificationRepository, "el registro de avisos");
		this.recipients = requireDependency(recipients, "la búsqueda del destinatario");
		this.bookTitles = requireDependency(bookTitles, "la búsqueda del título");
		this.templates = requireDependency(templates, "el catálogo de mensajes");
		this.email = requireDependency(email, "el envío de correos");
	}

	@Override
	public NotificationOutcomeDomain execute(final SendNotificationDomain domain) {
		final SendNotificationDomain notice = ObjectHelper.getDefaultValue(
				domain, new SendNotificationDomain("", null, null, null, null));
		notice.validate();

		final String key = notice.idempotencyKey();
		if (!notificationRepository.findByCriteria(NotificationCriteria.byKey(key)).isEmpty()) {
			return new NotificationOutcomeDomain(NotificationOutcomeDomain.ALREADY_SENT);
		}

		final RecipientEntity recipient = recipients.find(notice.user());
		if (!recipient.hasEmail()) {
			registerOutcome(notice, NotificationOutcomeDomain.WITHOUT_EMAIL, "");
			return new NotificationOutcomeDomain(NotificationOutcomeDomain.WITHOUT_EMAIL);
		}

		final String title = bookTitles.titleOf(notice.book());
		final String providerMessageId = email.send(new EmailEntity(
				recipient.email(),
				recipient.name(),
				notice.fill(templates.textOf(notice.subjectKey()), recipient.name(), title),
				notice.fill(templates.textOf(notice.bodyKey()), recipient.name(), title)));

		registerOutcome(notice, NotificationOutcomeDomain.SENT, providerMessageId);
		return new NotificationOutcomeDomain(NotificationOutcomeDomain.SENT);
	}

	private void registerOutcome(final SendNotificationDomain notice, final String status, final String providerMessageId) {
		notificationRepository.create(new NotificationEntity(
				UUIDHelper.generateNew(),
				notice.idempotencyKey(),
				notice.kind(),
				notice.user(),
				NotificationEntity.CHANNEL_EMAIL,
				status,
				providerMessageId));
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso SendNotification se creó sin " + name + ".");
		}
		return dependency;
	}

}
