package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;
import co.com.bookhive.shared.services.output.ports.repositories.FindRepository;
import co.com.bookhive.shared.services.output.ports.repositories.UpdateRepository;

public interface BookCopyRepository
		extends FindRepository<UUID, BookCopyEntity>,
		FindByCriteriaRepository<BookCopyEntity, BookCopyCriteria>,
		UpdateRepository<BookCopyEntity> {

}
