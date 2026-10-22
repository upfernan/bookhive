package co.com.bookhive.catalog.services.output.ports;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.catalog.services.output.ports.entities.BookCopiesEntity;

public interface BookCopiesPort {

	List<BookCopiesEntity> countByBooks(List<UUID> books);

}
