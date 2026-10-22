package co.com.bookhive.shared.services.output.ports.repositories;

import java.util.List;

public interface FindByCriteriaRepository<E, C> {

	List<E> findByCriteria(C criteria);

}
