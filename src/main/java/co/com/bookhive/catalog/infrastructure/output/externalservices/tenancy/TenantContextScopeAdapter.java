package co.com.bookhive.catalog.infrastructure.output.externalservices.tenancy;

import org.springframework.stereotype.Component;

import co.com.bookhive.catalog.services.output.ports.TenantScopePort;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

@Component
public class TenantContextScopeAdapter implements TenantScopePort {

	@Override
	public void within(final String schema, final Runnable action) {
		final String previous = TenantContext.get();
		TenantContext.set(schema);
		try {
			action.run();
		} finally {
			if (TenantContext.SCHEMA_WITHOUT_TENANT.equals(previous)) {
				TenantContext.clear();
			} else {
				TenantContext.set(previous);
			}
		}
	}

}
