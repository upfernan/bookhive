package co.com.bookhive.circulation.services.input.adapters;

import java.util.List;
import co.com.bookhive.circulation.domain.user.searchreaders.FoundReaderDomain;
import co.com.bookhive.circulation.domain.user.searchreaders.SearchReadersDomain;
import co.com.bookhive.circulation.services.input.dto.request.SearchReadersRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FoundReaderResponseDTO;
import co.com.bookhive.circulation.services.input.ports.SearchReadersInputPort;
import co.com.bookhive.circulation.services.usecase.user.searchreaders.SearchReadersUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class SearchReadersInputAdapter implements SearchReadersInputPort {

	private final SearchReadersUseCase useCase;

	public SearchReadersInputAdapter(final SearchReadersUseCase useCase) {
		super();
		this.useCase = useCase;
	}

	@Override
	public List<FoundReaderResponseDTO> execute(final SearchReadersRequestDTO request) {
		final SearchReadersRequestDTO safeRequest = ObjectHelper.getDefaultValue(request, new SearchReadersRequestDTO(""));

		final List<FoundReaderDomain> found = ObjectHelper.getDefaultValue(
				useCase.execute(new SearchReadersDomain(safeRequest.text())), List.of());

		return found.stream()
				.map(reader -> new FoundReaderResponseDTO(reader.id(), reader.name(), reader.email()))
				.toList();
	}

}
