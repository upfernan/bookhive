package co.com.bookhive.catalog.services.input.adapters;

import co.com.bookhive.catalog.domain.book.findbooktitle.FindBookTitleDomain;
import co.com.bookhive.catalog.services.input.dto.request.FindBookTitleRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.BookTitleResponseDTO;
import co.com.bookhive.catalog.services.input.ports.FindBookTitleInputPort;
import co.com.bookhive.catalog.services.usecase.book.findbooktitle.FindBookTitleUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class FindBookTitleInputAdapter implements FindBookTitleInputPort {

	private final FindBookTitleUseCase useCase;

	public FindBookTitleInputAdapter(final FindBookTitleUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public BookTitleResponseDTO execute(final FindBookTitleRequestDTO request) {
		final FindBookTitleRequestDTO safeRequest = ObjectHelper.getDefaultValue(request, new FindBookTitleRequestDTO(null));
		final FindBookTitleDomain found = useCase.execute(new FindBookTitleDomain(safeRequest.book(), ""));
		return new BookTitleResponseDTO(found.book(), found.title());
	}

}
