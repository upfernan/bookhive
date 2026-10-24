package co.com.bookhive.catalog.infrastructure.output.externalservices.circulation;

import java.util.UUID;

import org.springframework.stereotype.Component;

import co.com.bookhive.catalog.services.output.ports.BookCopyStatusPort;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopyStatusEntity;
import co.com.bookhive.circulation.services.input.dto.request.FindBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.FindBookCopyResponseDTO;
import co.com.bookhive.circulation.services.input.ports.FindBookCopyInputPort;

@Component
public class CirculationBookCopyStatusAdapter implements BookCopyStatusPort {

	private final FindBookCopyInputPort findBookCopy;

	public CirculationBookCopyStatusAdapter(final FindBookCopyInputPort findBookCopy) {
		super();
		this.findBookCopy = findBookCopy;
	}

	@Override
	public BookCopyStatusEntity findByBarcode(final String barcode, final UUID user) {

		final FindBookCopyResponseDTO found = findBookCopy.execute(new FindBookCopyRequestDTO(barcode, user));

		return new BookCopyStatusEntity(
				found.id(),
				found.book(),
				found.barcode(),
				found.available(),
				found.lendable(),
				found.blocks());
	}

}
