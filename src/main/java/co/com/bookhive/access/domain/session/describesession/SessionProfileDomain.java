package co.com.bookhive.access.domain.session.describesession;

import java.util.List;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record SessionProfileDomain(String email, String institution, List<String> roles, List<String> permissions) {

	public SessionProfileDomain {
		email = TextHelper.getDefaultValue(email).trim();
		institution = TextHelper.getDefaultValue(institution).trim();
		roles = ObjectHelper.getDefaultValue(roles, List.<String>of()).stream().sorted().toList();
		permissions = ObjectHelper.getDefaultValue(permissions, List.<String>of()).stream().sorted().toList();
	}

}
