package co.com.bookhive.circulation.domain.user.findusercontact;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FindUserContactDomain(UUID user, String name, String email) {

	public FindUserContactDomain {
		user = UUIDHelper.getDefaultValue(user);
		name = TextHelper.getDefaultValue(name);
		email = TextHelper.getDefaultValue(email);
	}

}
