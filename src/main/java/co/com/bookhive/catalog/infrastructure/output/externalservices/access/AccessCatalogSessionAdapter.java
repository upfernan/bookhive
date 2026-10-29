package co.com.bookhive.catalog.infrastructure.output.externalservices.access;

import java.util.UUID;
import org.springframework.stereotype.Component;
import co.com.bookhive.access.services.input.ports.CurrentSessionInputPort;
import co.com.bookhive.catalog.services.output.ports.CatalogSessionPort;

@Component
public class AccessCatalogSessionAdapter implements CatalogSessionPort {

	private final CurrentSessionInputPort currentSession;

	public AccessCatalogSessionAdapter(final CurrentSessionInputPort currentSession) {
		super();
		this.currentSession = currentSession;
	}

	@Override
	public UUID institution() {
		return currentSession.execute().institution();
	}

	@Override
	public UUID person() {
		return currentSession.execute().librarian();
	}

}
