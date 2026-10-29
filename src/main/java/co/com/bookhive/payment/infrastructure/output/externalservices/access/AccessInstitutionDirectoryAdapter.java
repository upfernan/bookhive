package co.com.bookhive.payment.infrastructure.output.externalservices.access;

import java.util.List;
import org.springframework.stereotype.Component;
import co.com.bookhive.access.services.input.ports.ListActiveSchemasInputPort;
import co.com.bookhive.payment.services.output.ports.InstitutionDirectoryPort;

@Component("paymentInstitutionDirectoryAdapter")
public class AccessInstitutionDirectoryAdapter implements InstitutionDirectoryPort {

	private final ListActiveSchemasInputPort listActiveSchemas;

	public AccessInstitutionDirectoryAdapter(final ListActiveSchemasInputPort listActiveSchemas) {
		super();
		this.listActiveSchemas = listActiveSchemas;
	}

	@Override
	public List<String> activeSchemas() {
		return listActiveSchemas.execute().schemas();
	}

}
