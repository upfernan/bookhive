package co.com.bookhive.notification.infrastructure.output.repositories.structured.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.com.bookhive.notification.infrastructure.output.repositories.structured.jpa.entities.NotificationJpaEntity;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

	List<NotificationJpaEntity> findByIdempotencyKey(String idempotencyKey);

}
