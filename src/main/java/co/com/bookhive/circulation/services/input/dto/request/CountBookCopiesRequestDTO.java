package co.com.bookhive.circulation.services.input.dto.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record CountBookCopiesRequestDTO(List<UUID> books) {

	public CountBookCopiesRequestDTO {
		books = Collections.unmodifiableList(new ArrayList<>(ObjectHelper.getDefaultValue(books, List.<UUID>of())));
	}

}
