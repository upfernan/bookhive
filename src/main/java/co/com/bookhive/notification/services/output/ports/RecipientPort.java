package co.com.bookhive.notification.services.output.ports;

import java.util.UUID;
import co.com.bookhive.notification.services.output.ports.entities.RecipientEntity;

public interface RecipientPort {

	RecipientEntity find(UUID user);

}
