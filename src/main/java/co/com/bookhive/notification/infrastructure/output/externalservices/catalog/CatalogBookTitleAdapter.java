package co.com.bookhive.notification.infrastructure.output.externalservices.catalog;

import java.util.UUID;
import org.springframework.stereotype.Component;
import co.com.bookhive.catalog.services.input.dto.request.FindBookTitleRequestDTO;
import co.com.bookhive.catalog.services.input.ports.FindBookTitleInputPort;
import co.com.bookhive.notification.services.output.ports.BookTitlePort;

@Component
public class CatalogBookTitleAdapter implements BookTitlePort {

	private final FindBookTitleInputPort findBookTitle;

	public CatalogBookTitleAdapter(final FindBookTitleInputPort findBookTitle) {
		super();
		this.findBookTitle = findBookTitle;
	}

	@Override
	public String titleOf(final UUID book) {
		return findBookTitle.execute(new FindBookTitleRequestDTO(book)).title();
	}

}
