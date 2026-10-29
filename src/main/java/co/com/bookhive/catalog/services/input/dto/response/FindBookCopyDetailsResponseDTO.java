package co.com.bookhive.catalog.services.input.dto.response;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FindBookCopyDetailsResponseDTO(
		UUID id,
		String barcode,
		String title,
		String authors,
		String branch,
		boolean available,
		boolean lendable,
		List<String> blocks) {

	public FindBookCopyDetailsResponseDTO {
		id = UUIDHelper.getDefaultValue(id);
		barcode = TextHelper.getDefaultValue(barcode);
		title = TextHelper.getDefaultValue(title);
		authors = TextHelper.getDefaultValue(authors);
		branch = TextHelper.getDefaultValue(branch);
		blocks = List.copyOf(ObjectHelper.getDefaultValue(blocks, List.of()));
	}

	public FindBookCopyDetailsResponseDTO withBlocks(final List<String> newBlocks) {
		return new FindBookCopyDetailsResponseDTO(id, barcode, title, authors, branch, available, lendable, newBlocks);
	}

}
