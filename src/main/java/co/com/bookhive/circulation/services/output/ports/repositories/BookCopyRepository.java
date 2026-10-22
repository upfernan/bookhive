package co.com.bookhive.circulation.services.output.ports.repositories;

import co.com.bookhive.shared.services.output.ports.repositories.UpdateRepository;

import co.com.bookhive.shared.services.output.ports.repositories.FindRepository;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;

public interface BookCopyRepository
		extends FindRepository<UUID, BookCopyEntity>,
		UpdateRepository<BookCopyEntity> {

}
