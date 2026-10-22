package co.com.bookhive.catalog.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.CatalogChangeJpaRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.CatalogChangeRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.CatalogChangeCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogChangeEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

@Component
public class CatalogChangeRepositoryAdapter implements CatalogChangeRepository {

	private final CatalogChangeJpaRepository repository;

	public CatalogChangeRepositoryAdapter(final CatalogChangeJpaRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public List<CatalogChangeEntity> findByCriteria(final CatalogChangeCriteria criteria) {
		final CatalogChangeCriteria safeCriteria =
				ObjectHelper.getDefaultValue(criteria, CatalogChangeCriteria.pending(CatalogChangeCriteria.MAX_LIMIT));
		return repository.findPending(safeCriteria.limit()).stream()
				.map(change -> new CatalogChangeEntity(change.getId(), change.getBook()))
				.toList();
	}

	@Override
	public void markSent(final List<UUID> changes) {
		final List<UUID> safeChanges = ObjectHelper.getDefaultValue(changes, List.<UUID>of());
		if (!safeChanges.isEmpty()) {
			repository.markSent(safeChanges);
		}
	}

}
