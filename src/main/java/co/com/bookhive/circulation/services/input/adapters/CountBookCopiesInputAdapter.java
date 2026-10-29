package co.com.bookhive.circulation.services.input.adapters;

import java.util.List;
import co.com.bookhive.circulation.domain.bookcopy.countbookcopies.CountBookCopiesDomain;
import co.com.bookhive.circulation.services.input.dto.request.CountBookCopiesRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.BookCopiesDTO;
import co.com.bookhive.circulation.services.input.dto.response.CountBookCopiesResponseDTO;
import co.com.bookhive.circulation.services.input.ports.CountBookCopiesInputPort;
import co.com.bookhive.circulation.services.usecase.bookcopy.countbookcopies.CountBookCopiesUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class CountBookCopiesInputAdapter implements CountBookCopiesInputPort {

	private final CountBookCopiesUseCase useCase;

	public CountBookCopiesInputAdapter(final CountBookCopiesUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public CountBookCopiesResponseDTO execute(final CountBookCopiesRequestDTO request) {
		final CountBookCopiesRequestDTO safeRequest =
				ObjectHelper.getDefaultValue(request, new CountBookCopiesRequestDTO(List.of()));

		return new CountBookCopiesResponseDTO(useCase.execute(new CountBookCopiesDomain(safeRequest.books()))
				.stream()
				.map(count -> new BookCopiesDTO(count.book(), count.available(), count.total()))
				.toList());
	}

}
