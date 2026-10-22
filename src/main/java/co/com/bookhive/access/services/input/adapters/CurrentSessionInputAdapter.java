package co.com.bookhive.access.services.input.adapters;

import co.com.bookhive.access.services.input.dto.response.CurrentSessionResponseDTO;
import co.com.bookhive.access.services.input.ports.CurrentSessionInputPort;
import co.com.bookhive.access.services.output.ports.SessionContextPort;

public final class CurrentSessionInputAdapter implements CurrentSessionInputPort {

	private final SessionContextPort sessionContext;

	public CurrentSessionInputAdapter(final SessionContextPort sessionContext) {
		super();
		this.sessionContext = sessionContext;
	}

	@Override
	public CurrentSessionResponseDTO execute() {
		return new CurrentSessionResponseDTO(sessionContext.getInstitution(), sessionContext.getLibrarian());
	}

}
