package co.com.bookhive.catalog.infrastructure.output.externalservices.circulation;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import co.com.bookhive.catalog.services.output.ports.BookCopyInventoryPort;
import co.com.bookhive.circulation.services.input.dto.request.RegisterBookCopiesRequestDTO;
import co.com.bookhive.circulation.services.input.ports.RegisterBookCopiesInputPort;
import co.com.bookhive.circulation.services.input.ports.RetireBookCopiesInputPort;

@Component
public class CirculationBookCopyInventoryAdapter implements BookCopyInventoryPort {

	private final RegisterBookCopiesInputPort registerCopies;
	private final RetireBookCopiesInputPort retireCopies;

	public CirculationBookCopyInventoryAdapter(
			final RegisterBookCopiesInputPort registerCopies, final RetireBookCopiesInputPort retireCopies) {
		super();
		this.registerCopies = registerCopies;
		this.retireCopies = retireCopies;
	}

	@Override
	public int register(final UUID book, final UUID branch, final List<String> barcodes) {
		return registerCopies.execute(new RegisterBookCopiesRequestDTO(book, branch, barcodes));
	}

	@Override
	public int retireAll(final UUID book) {
		return retireCopies.execute(book);
	}

}
