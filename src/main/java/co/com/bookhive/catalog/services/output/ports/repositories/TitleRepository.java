package co.com.bookhive.catalog.services.output.ports.repositories;

import java.util.Optional;
import java.util.UUID;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogAdminEntities.TitleEntity;

public interface TitleRepository {

	Optional<TitleEntity> findTitle(UUID id);

	boolean isbnTaken(String isbn, UUID except);

	void createTitle(TitleEntity title);

	void updateTitle(TitleEntity title);

	boolean withdrawTitle(UUID id);

}
