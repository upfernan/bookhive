package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.ReservationJpaEntity;

@Repository
public interface ReservationJpaRepository
		extends JpaRepository<ReservationJpaEntity, UUID>,
		JpaSpecificationExecutor<ReservationJpaEntity> {

	@Transactional
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("UPDATE ReservationJpaEntity r SET r.status = :newStatus WHERE r.id = :id AND r.status = :expectedStatus")
	int changeStatus(
			@Param("id") UUID id,
			@Param("expectedStatus") String expectedStatus,
			@Param("newStatus") String newStatus);

}
