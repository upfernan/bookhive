package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.UserJpaEntity;

public interface UserLifecycleJpaRepository extends Repository<UserJpaEntity, UUID> {

	interface AccountRow {

		UUID getId();

		String getStatus();

		Instant getDeactivatedAt();

	}

	@Query(value = "SELECT id, status, deactivated_at AS deactivatedAt FROM library_user WHERE id = :id FOR UPDATE", nativeQuery = true)
	List<AccountRow> lock(@Param("id") UUID id);

	@Query(value = "SELECT count(*) FROM loan l WHERE l.user_id = :id"
			+ " AND NOT EXISTS (SELECT 1 FROM loan_return r WHERE r.loan_id = l.id)", nativeQuery = true)
	long activeLoans(@Param("id") UUID id);

	@Query(value = "SELECT count(*) FROM fine f WHERE f.user_id = :id"
			+ " AND NOT EXISTS (SELECT 1 FROM fine_settlement s WHERE s.fine_id = f.id)", nativeQuery = true)
	long pendingFines(@Param("id") UUID id);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "UPDATE library_user SET status = :status,"
			+ " deactivated_at = CASE WHEN :status = 'RETIRADO' THEN now() ELSE NULL END WHERE id = :id", nativeQuery = true)
	int changeStatus(@Param("id") UUID id, @Param("status") String status);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "UPDATE library_user SET name = 'Usuario depurado', email = 'depurado-' || id::text || '@bookhive.invalid',"
			+ " status = 'DEPURADO', deactivated_at = coalesce(deactivated_at, now()) WHERE id = :id", nativeQuery = true)
	int anonymize(@Param("id") UUID id);

	@Query(value = "SELECT id FROM library_user WHERE status = 'RETIRADO'"
			+ " AND deactivated_at < now() - make_interval(months => :months) ORDER BY deactivated_at", nativeQuery = true)
	List<UUID> expiredInactive(@Param("months") int months);

	@Query(value = "SELECT retention_months FROM institution_parameters ORDER BY id LIMIT 1", nativeQuery = true)
	List<Integer> retentionMonths();

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "UPDATE institution_parameters SET retention_months = :months", nativeQuery = true)
	int changeRetention(@Param("months") int months);

}
