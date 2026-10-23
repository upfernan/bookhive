package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.BookCopyJpaEntity;

@Repository
public interface BookCopyJpaRepository
		extends JpaRepository<BookCopyJpaEntity, UUID>,
		JpaSpecificationExecutor<BookCopyJpaEntity> {

	@Transactional
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("UPDATE BookCopyJpaEntity c SET c.status = :newStatus WHERE c.id = :id AND c.status = :expectedStatus")
	int changeStatus(
			@Param("id") UUID id,
			@Param("expectedStatus") String expectedStatus,
			@Param("newStatus") String newStatus);

}
