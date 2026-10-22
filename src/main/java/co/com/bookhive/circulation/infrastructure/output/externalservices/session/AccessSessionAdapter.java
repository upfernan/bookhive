package co.com.bookhive.circulation.infrastructure.output.externalservices.session;

import java.util.UUID;

import org.springframework.stereotype.Component;

import co.com.bookhive.access.services.input.ports.CurrentSessionInputPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;

@Component
public class AccessSessionAdapter implements SessionContextPort {

	private final CurrentSessionInputPort currentSession;

	public AccessSessionAdapter(final CurrentSessionInputPort currentSession) {
		super();
		this.currentSession = currentSession;
	}

	@Override
	public UUID getInstitution() {
		return currentSession.execute().institution();
	}

	@Override
	public UUID getLibrarian() {
		return currentSession.execute().librarian();
	}

}
