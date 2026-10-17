package co.com.bookhive.circulation.infrastructure.output.repositories.structured.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantSchemaResolver implements CurrentTenantIdentifierResolver<String> {

	@Override
	public String resolveCurrentTenantIdentifier() {
		return TenantContext.get();
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return false;
	}

}
