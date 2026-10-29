package co.com.bookhive.circulation.services.input.dto.response;

import java.util.List;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record CountBookCopiesResponseDTO(List<BookCopiesDTO> books) {

	public CountBookCopiesResponseDTO {
		books = List.copyOf(ObjectHelper.getDefaultValue(books, List.of()));
	}

}
