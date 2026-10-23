package co.com.bookhive.circulation.domain.bookcopy.findbookcopy;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record FoundBookCopyDomain(
		UUID id,
		UUID book,
		String barcode,
		boolean available,
		boolean lendable,
		List<String> blocks) {

	public FoundBookCopyDomain {
		id = UUIDHelper.getDefaultValue(id);
		book = UUIDHelper.getDefaultValue(book);
		barcode = TextHelper.getDefaultValue(barcode);
		blocks = List.copyOf(ObjectHelper.getDefaultValue(blocks, List.of()));
	}

}
