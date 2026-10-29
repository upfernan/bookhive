package co.com.bookhive.notification.infrastructure.output.externalservices.circulation;

import java.util.UUID;
import org.springframework.stereotype.Component;
import co.com.bookhive.circulation.services.input.dto.request.FindUserContactRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.UserContactResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindUserContactInputPort;
import co.com.bookhive.notification.services.output.ports.RecipientPort;
import co.com.bookhive.notification.services.output.ports.entities.RecipientEntity;

@Component
public class CirculationRecipientAdapter implements RecipientPort {

	private final FindUserContactInputPort findUserContact;

	public CirculationRecipientAdapter(final FindUserContactInputPort findUserContact) {
		super();
		this.findUserContact = findUserContact;
	}

	@Override
	public RecipientEntity find(final UUID user) {
		final UserContactResponseDTO contact = findUserContact.execute(new FindUserContactRequestDTO(user));
		return new RecipientEntity(contact.name(), contact.email());
	}

}
