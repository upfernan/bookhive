package co.com.bookhive.shared.services.input.adapters;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.shared.domain.message.resolvemessage.ResolveMessageDomain;
import co.com.bookhive.shared.services.input.dto.request.ResolveMessageRequestDTO;
import co.com.bookhive.shared.services.input.dto.response.ResolveMessageResponseDTO;
import co.com.bookhive.shared.services.input.ports.ResolveMessageInputPort;
import co.com.bookhive.shared.services.usecase.message.resolvemessage.ResolveMessageUseCase;

public final class ResolveMessageInputAdapter implements ResolveMessageInputPort {

	private final ResolveMessageUseCase useCase;

	public ResolveMessageInputAdapter(final ResolveMessageUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public ResolveMessageResponseDTO execute(final ResolveMessageRequestDTO input) {
		final ResolveMessageRequestDTO request = ObjectHelper.getDefaultValue(input, new ResolveMessageRequestDTO(""));
		final ResolveMessageDomain message = useCase.execute(new ResolveMessageDomain(request.messageKey(), ""));
		return new ResolveMessageResponseDTO(message.getText());
	}

}
