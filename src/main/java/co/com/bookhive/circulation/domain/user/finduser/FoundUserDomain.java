package co.com.bookhive.circulation.domain.user.finduser;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FoundUserDomain(
		UUID id,
		String name,
		String email,
		int activeLoans,
		int maxLoans,
		int pendingFines,
		List<String> blocks) {

	public FoundUserDomain {
		id = UUIDHelper.getDefaultValue(id);
		name = TextHelper.getDefaultValue(name);
		email = TextHelper.getDefaultValue(email);
		activeLoans = Math.max(activeLoans, 0);
		maxLoans = Math.max(maxLoans, 0);
		pendingFines = Math.max(pendingFines, 0);
		blocks = List.copyOf(ObjectHelper.getDefaultValue(blocks, List.of()));
	}

}
