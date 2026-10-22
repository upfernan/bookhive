package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.services.input.dto.response.ListActiveSchemasResponseDTO;
import co.com.bookhive.access.services.input.ports.ListActiveSchemasInputPort;
import co.com.bookhive.access.services.usecase.institution.listactiveschemas.ListActiveSchemasUseCase;

public final class ListActiveSchemasInputAdapter implements ListActiveSchemasInputPort {

	private final ListActiveSchemasUseCase useCase;

	public ListActiveSchemasInputAdapter(final ListActiveSchemasUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public ListActiveSchemasResponseDTO execute() {
		return new ListActiveSchemasResponseDTO(useCase.execute().schemas());
	}

}
