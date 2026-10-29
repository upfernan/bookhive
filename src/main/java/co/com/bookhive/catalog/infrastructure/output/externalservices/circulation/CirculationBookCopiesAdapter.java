package co.com.bookhive.catalog.infrastructure.output.externalservices.circulation;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import co.com.bookhive.catalog.services.output.ports.BookCopiesPort;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopiesEntity;
import co.com.bookhive.circulation.services.input.dto.request.CountBookCopiesRequestDTO;
import co.com.bookhive.circulation.services.input.ports.CountBookCopiesInputPort;

@Component
public class CirculationBookCopiesAdapter implements BookCopiesPort {

	private final CountBookCopiesInputPort countBookCopies;

	public CirculationBookCopiesAdapter(final CountBookCopiesInputPort countBookCopies) {
		super();
		this.countBookCopies = countBookCopies;
	}

	@Override
	public List<BookCopiesEntity> countByBooks(final List<UUID> books) {
		return countBookCopies.execute(new CountBookCopiesRequestDTO(books)).books().stream()
				.map(copies -> new BookCopiesEntity(copies.book(), copies.available(), copies.total()))
				.toList();
	}

}
