package co.com.bookhive.notification.services.output.ports;

import co.com.bookhive.notification.services.output.ports.entities.EmailEntity;

public interface EmailPort {

	String send(EmailEntity email);

}
