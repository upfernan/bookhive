package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.com.bookhive.notification.services.input.adapters.SendNotificationInputAdapter;
import co.com.bookhive.notification.services.input.ports.SendNotificationInputPort;
import co.com.bookhive.notification.services.output.ports.BookTitlePort;
import co.com.bookhive.notification.services.output.ports.EmailPort;
import co.com.bookhive.notification.services.output.ports.MessageTemplatePort;
import co.com.bookhive.notification.services.output.ports.RecipientPort;
import co.com.bookhive.notification.services.output.ports.repositories.NotificationRepository;
import co.com.bookhive.notification.services.usecase.notification.sendnotification.SendNotificationUseCase;
import co.com.bookhive.notification.services.usecase.notification.sendnotification.impl.SendNotificationUseCaseImpl;

@Configuration
public class NotificationConfiguration {

	@Bean
	SendNotificationUseCase sendNotificationUseCase(
			final NotificationRepository notificationRepository,
			final RecipientPort recipients,
			final BookTitlePort bookTitles,
			final MessageTemplatePort templates,
			final EmailPort email) {

		return new SendNotificationUseCaseImpl(notificationRepository, recipients, bookTitles, templates, email);
	}

	@Bean
	SendNotificationInputPort sendNotificationInputPort(final SendNotificationUseCase sendNotificationUseCase) {
		return new SendNotificationInputAdapter(sendNotificationUseCase);
	}

}
