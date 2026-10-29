package co.com.bookhive.circulation.services.input.dto.request;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class LendBookRequestDTO {

	private UUID bookCopy;
	private UUID user;

	public LendBookRequestDTO() {
		super();
		setBookCopy(UUIDHelper.getDefaultValue());
		setUser(UUIDHelper.getDefaultValue());
	}

	public LendBookRequestDTO(final UUID bookCopy, final UUID user) {
		super();
		setBookCopy(bookCopy);
		setUser(user);
	}

	public UUID getBookCopy() {
		return bookCopy;
	}

	public UUID getUser() {
		return user;
	}

	private void setBookCopy(final UUID bookCopy) {
		this.bookCopy = UUIDHelper.getDefaultValue(bookCopy);
	}

	private void setUser(final UUID user) {
		this.user = UUIDHelper.getDefaultValue(user);
	}

}
