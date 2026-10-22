package co.com.bookhive.catalog.services.output.ports.repositories;

import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

public interface BookRepository extends FindByCriteriaRepository<BookEntity, BookCriteria> {

	long countActive();

}
