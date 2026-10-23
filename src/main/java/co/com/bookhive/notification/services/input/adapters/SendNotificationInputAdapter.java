package co.com.bookhive.notification.services.input.adapters;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.notification.domain.notification.sendnotification.SendNotificationDomain;
import co.com.bookhive.notification.services.input.dto.request.SendNotificationRequestDTO;
import co.com.bookhive.notification.services.input.dto.response.SendNotificationResponseDTO;
import co.com.bookhive.notification.services.input.ports.SendNotificationInputPort;
import co.com.bookhive.notification.services.usecase.notification.sendnotification.SendNotificationUseCase;

public final class SendNotificationInputAdapter implements SendNotificationInputPort {

	private final SendNotificationUseCase useCase;

	public SendNotificationInputAdapter(final SendNotificationUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public SendNotificationResponseDTO execute(final SendNotificationRequestDTO request) {
		final SendNotificationRequestDTO safeRequest =
				ObjectHelper.getDefaultValue(request, new SendNotificationRequestDTO("", null, null, null, null));
		return new SendNotificationResponseDTO(useCase.execute(new SendNotificationDomain(
				safeRequest.kind(),
				safeRequest.user(),
				safeRequest.book(),
				safeRequest.loan(),
				safeRequest.dueDate())).status());
	}

}
