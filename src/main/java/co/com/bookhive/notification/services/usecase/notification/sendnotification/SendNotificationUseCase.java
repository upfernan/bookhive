package co.com.bookhive.notification.services.usecase.notification.sendnotification;

import co.com.bookhive.notification.domain.notification.sendnotification.NotificationOutcomeDomain;
import co.com.bookhive.notification.domain.notification.sendnotification.SendNotificationDomain;
import co.com.bookhive.shared.services.usecase.UseCaseWithReturn;

public interface SendNotificationUseCase extends UseCaseWithReturn<SendNotificationDomain, NotificationOutcomeDomain> {
}
