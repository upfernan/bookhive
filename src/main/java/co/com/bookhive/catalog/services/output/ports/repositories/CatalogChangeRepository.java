package co.com.bookhive.catalog.services.output.ports.repositories;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.catalog.services.output.ports.repositories.criteria.CatalogChangeCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogChangeEntity;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

public interface CatalogChangeRepository extends FindByCriteriaRepository<CatalogChangeEntity, CatalogChangeCriteria> {

	void markSent(List<UUID> changes);

}
