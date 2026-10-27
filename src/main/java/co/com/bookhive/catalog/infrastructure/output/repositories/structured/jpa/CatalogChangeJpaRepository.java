package co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.entities.CatalogChangeJpaEntity;


public interface CatalogChangeJpaRepository extends JpaRepository<CatalogChangeJpaEntity, UUID> {

	@Query(value = "SELECT id, book_id, changed_at FROM catalog_change WHERE sent_at IS NULL"
			+ " ORDER BY changed_at LIMIT :limit", nativeQuery = true)
	List<CatalogChangeJpaEntity> findPending(@Param("limit") int limit);

	@Transactional
	@Modifying
	@Query(value = "UPDATE catalog_change SET sent_at = now() WHERE id IN (:ids) AND sent_at IS NULL",
			nativeQuery = true)
	int markSent(@Param("ids") Collection<UUID> ids);

}
