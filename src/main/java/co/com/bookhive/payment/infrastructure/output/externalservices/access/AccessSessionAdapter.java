package co.com.bookhive.payment.infrastructure.output.externalservices.access;

import java.util.UUID;

import org.springframework.stereotype.Component;

import co.com.bookhive.access.services.input.ports.CurrentSessionInputPort;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.payment.services.output.ports.SessionContextPort;

@Component("paymentSessionAdapter")
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
	public UUID getPerson() {
		return currentSession.execute().librarian();
	}

	@Override
	public String getSchema() {
		final String schema = TenantContext.get();
		return TenantContext.SCHEMA_WITHOUT_TENANT.equals(schema) ? "" : schema;
	}

}
