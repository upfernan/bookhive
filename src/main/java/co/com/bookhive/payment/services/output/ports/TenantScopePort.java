package co.com.bookhive.payment.services.output.ports;

import java.util.function.Supplier;

public interface TenantScopePort {

	<T> T within(String schema, Supplier<T> action);

}
