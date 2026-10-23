package co.com.bookhive.circulation.services.output.ports;

import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;

public interface NotificationRequestPort {

	void request(NotificationRequestEntity notification);

}
