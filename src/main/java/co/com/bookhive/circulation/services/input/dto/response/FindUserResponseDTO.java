package co.com.bookhive.circulation.services.input.dto.response;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FindUserResponseDTO(
		UUID id,
		String name,
		String email,
		int activeLoans,
		int maxLoans,
		int pendingFines,
		List<String> blocks) {

	public FindUserResponseDTO {
		id = UUIDHelper.getDefaultValue(id);
		name = TextHelper.getDefaultValue(name);
		email = TextHelper.getDefaultValue(email);
		blocks = List.copyOf(ObjectHelper.getDefaultValue(blocks, List.of()));
	}

	public FindUserResponseDTO withBlocks(final List<String> newBlocks) {
		return new FindUserResponseDTO(id, name, email, activeLoans, maxLoans, pendingFines, newBlocks);
	}

}
