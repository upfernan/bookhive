package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.List;
import java.util.Optional;

import co.com.bookhive.circulation.services.output.ports.repositories.criteria.PageCriteria;

public interface FindRepository<I, E> {

	Optional<E> findById(I id);

	List<E> findAll(PageCriteria page);

}
