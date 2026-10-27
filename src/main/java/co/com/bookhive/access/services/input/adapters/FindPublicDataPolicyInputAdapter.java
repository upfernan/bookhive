package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.domain.datapolicy.findpublicdatapolicy.PublicDataPolicyDomain;
import co.com.bookhive.access.services.input.dto.response.PublicDataPolicyResponseDTO;
import co.com.bookhive.access.services.input.ports.FindPublicDataPolicyInputPort;
import co.com.bookhive.access.services.usecase.datapolicy.findpublicdatapolicy.FindPublicDataPolicyUseCase;

public final class FindPublicDataPolicyInputAdapter implements FindPublicDataPolicyInputPort {

	private final FindPublicDataPolicyUseCase useCase;

	public FindPublicDataPolicyInputAdapter(final FindPublicDataPolicyUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public PublicDataPolicyResponseDTO execute(final String domain) {
		final PublicDataPolicyDomain policy = useCase.execute(domain);
		return new PublicDataPolicyResponseDTO(policy.version(), policy.content(), policy.publishedAt());
	}

}
