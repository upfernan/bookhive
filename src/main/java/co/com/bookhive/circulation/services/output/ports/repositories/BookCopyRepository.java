package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;

public interface BookCopyRepository
		extends FindRepository<UUID, BookCopyEntity>,
		UpdateRepository<BookCopyEntity> {

}
