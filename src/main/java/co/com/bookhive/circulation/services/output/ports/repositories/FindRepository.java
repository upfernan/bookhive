package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.List;
import java.util.Optional;

public interface FindRepository<I, E> {

	Optional<E> findById(I id);

	List<E> findAll();

}
