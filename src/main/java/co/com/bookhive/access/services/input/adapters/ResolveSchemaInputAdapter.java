package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.services.input.dto.response.ResolveSchemaResponseDTO;
import co.com.bookhive.access.services.input.ports.ResolveSchemaInputPort;
import co.com.bookhive.access.services.usecase.session.resolveschema.ResolveSchemaUseCase;

public final class ResolveSchemaInputAdapter implements ResolveSchemaInputPort {

	private final ResolveSchemaUseCase useCase;

	public ResolveSchemaInputAdapter(final ResolveSchemaUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public ResolveSchemaResponseDTO execute() {
		return new ResolveSchemaResponseDTO(useCase.execute().schema());
	}

}
