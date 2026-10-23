package co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record BookCopyDetailsDomain(
		UUID id,
		String barcode,
		String title,
		String authors,
		String branch,
		boolean available,
		boolean lendable,
		List<String> blocks) {

	public BookCopyDetailsDomain {
		id = UUIDHelper.getDefaultValue(id);
		barcode = TextHelper.getDefaultValue(barcode);
		title = TextHelper.getDefaultValue(title);
		authors = TextHelper.getDefaultValue(authors);
		branch = TextHelper.getDefaultValue(branch);
		blocks = List.copyOf(ObjectHelper.getDefaultValue(blocks, List.of()));
	}

}
