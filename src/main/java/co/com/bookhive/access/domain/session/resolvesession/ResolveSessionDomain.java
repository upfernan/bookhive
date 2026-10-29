package co.com.bookhive.access.domain.session.resolvesession;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record ResolveSessionDomain(UUID person, UUID institution) {

	public ResolveSessionDomain {
		person = UUIDHelper.getDefaultValue(person);
		institution = UUIDHelper.getDefaultValue(institution);
	}

}
