package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.domain.datapolicy.acceptdatapolicy.AcceptDataPolicyDomain;
import co.com.bookhive.access.services.input.dto.request.AcceptDataPolicyRequestDTO;
import co.com.bookhive.access.services.input.dto.response.DataPolicyResponseDTO;
import co.com.bookhive.access.services.input.ports.AcceptDataPolicyInputPort;
import co.com.bookhive.access.services.usecase.datapolicy.acceptdatapolicy.AcceptDataPolicyUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class AcceptDataPolicyInputAdapter implements AcceptDataPolicyInputPort {

	private final AcceptDataPolicyUseCase useCase;

	public AcceptDataPolicyInputAdapter(final AcceptDataPolicyUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public DataPolicyResponseDTO execute(final AcceptDataPolicyRequestDTO request) {
		final int version = ObjectHelper.isNull(request) ? 0 : request.version();
		return DataPolicyResponseDTO.of(useCase.execute(new AcceptDataPolicyDomain(version)));
	}

}
