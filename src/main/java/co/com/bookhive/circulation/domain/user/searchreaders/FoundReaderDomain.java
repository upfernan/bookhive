package co.com.bookhive.circulation.domain.user.searchreaders;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FoundReaderDomain(UUID id, String name, String email) {

	public FoundReaderDomain {
		id = UUIDHelper.getDefaultValue(id);
		name = TextHelper.getDefaultValue(name);
		email = TextHelper.getDefaultValue(email);
	}

}
