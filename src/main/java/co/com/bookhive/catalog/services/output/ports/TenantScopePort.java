package co.com.bookhive.catalog.services.output.ports;

public interface TenantScopePort {

	void within(String schema, Runnable action);

}
