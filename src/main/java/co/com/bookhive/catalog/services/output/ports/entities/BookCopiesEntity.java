package co.com.bookhive.catalog.services.output.ports.entities;

import java.util.UUID;

import co.com.bookhive.crosscutting.helper.UUIDHelper;

public record BookCopiesEntity(UUID book, int available, int total) {

	public BookCopiesEntity {
		book = UUIDHelper.getDefaultValue(book);
	}

}
