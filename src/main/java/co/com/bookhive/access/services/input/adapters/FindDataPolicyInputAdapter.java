package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.services.input.dto.response.DataPolicyResponseDTO;
import co.com.bookhive.access.services.input.ports.FindDataPolicyInputPort;
import co.com.bookhive.access.services.usecase.datapolicy.finddatapolicy.FindDataPolicyUseCase;

public final class FindDataPolicyInputAdapter implements FindDataPolicyInputPort {

	private final FindDataPolicyUseCase useCase;

	public FindDataPolicyInputAdapter(final FindDataPolicyUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public DataPolicyResponseDTO execute() {
		return DataPolicyResponseDTO.of(useCase.execute());
	}

}
