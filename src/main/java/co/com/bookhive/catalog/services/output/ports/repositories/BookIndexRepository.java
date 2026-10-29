package co.com.bookhive.catalog.services.output.ports.repositories;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookSearchCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookSearchPageEntity;

public interface BookIndexRepository {

	BookSearchPageEntity search(BookSearchCriteria criteria);

	void save(List<BookEntity> books);

	void delete(List<UUID> books);

}
