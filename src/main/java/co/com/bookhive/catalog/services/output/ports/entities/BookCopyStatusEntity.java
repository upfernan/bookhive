package co.com.bookhive.catalog.services.output.ports.entities;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record BookCopyStatusEntity(
		UUID id,
		UUID book,
		String barcode,
		boolean available,
		boolean lendable,
		List<String> blocks) {

	public BookCopyStatusEntity {
		id = UUIDHelper.getDefaultValue(id);
		book = UUIDHelper.getDefaultValue(book);
		barcode = TextHelper.getDefaultValue(barcode);
		blocks = List.copyOf(ObjectHelper.getDefaultValue(blocks, List.of()));
	}

}
