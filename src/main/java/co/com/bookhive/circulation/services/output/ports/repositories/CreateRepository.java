package co.com.bookhive.circulation.services.output.ports.repositories;

public interface CreateRepository<E> {

	void create(E entity);

}
