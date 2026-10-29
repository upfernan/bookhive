package co.com.bookhive.payment.infrastructure.output.externalservices.tenancy;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import co.com.bookhive.crosscutting.tenancy.TenantContext;
import co.com.bookhive.payment.services.output.ports.TenantScopePort;

@Component("paymentTenantScopeAdapter")
public class TenantTransactionScopeAdapter implements TenantScopePort {

	private final TransactionTemplate transactions;

	public TenantTransactionScopeAdapter(final PlatformTransactionManager transactionManager) {
		super();
		this.transactions = new TransactionTemplate(transactionManager);
	}

	@Override
	public <T> T within(final String schema, final Supplier<T> action) {
		final String previous = TenantContext.get();
		TenantContext.set(schema);
		try {
			return transactions.execute(status -> action.get());
		} finally {
			if (TenantContext.SCHEMA_WITHOUT_TENANT.equals(previous)) {
				TenantContext.clear();
			} else {
				TenantContext.set(previous);
			}
		}
	}

}
