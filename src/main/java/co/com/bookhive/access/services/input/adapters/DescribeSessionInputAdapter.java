package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.domain.session.describesession.SessionProfileDomain;
import co.com.bookhive.access.services.input.dto.response.SessionProfileResponseDTO;
import co.com.bookhive.access.services.input.ports.DescribeSessionInputPort;
import co.com.bookhive.access.services.usecase.session.describesession.DescribeSessionUseCase;

public final class DescribeSessionInputAdapter implements DescribeSessionInputPort {

	private final DescribeSessionUseCase useCase;

	public DescribeSessionInputAdapter(final DescribeSessionUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public SessionProfileResponseDTO execute() {
		final SessionProfileDomain profile = useCase.execute();
		return new SessionProfileResponseDTO(profile.email(), profile.institution(), profile.roles(), profile.permissions());
	}

}
