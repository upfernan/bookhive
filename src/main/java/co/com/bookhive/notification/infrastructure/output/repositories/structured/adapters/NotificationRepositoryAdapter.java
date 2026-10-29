package co.com.bookhive.notification.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import org.springframework.stereotype.Component;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.notification.infrastructure.output.repositories.structured.jpa.NotificationJpaRepository;
import co.com.bookhive.notification.infrastructure.output.repositories.structured.jpa.entities.NotificationJpaEntity;
import co.com.bookhive.notification.services.output.ports.repositories.NotificationRepository;
import co.com.bookhive.notification.services.output.ports.repositories.criteria.NotificationCriteria;
import co.com.bookhive.notification.services.output.ports.repositories.entities.NotificationEntity;

@Component
public class NotificationRepositoryAdapter implements NotificationRepository {

	private final NotificationJpaRepository repository;

	public NotificationRepositoryAdapter(final NotificationJpaRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public List<NotificationEntity> findByCriteria(final NotificationCriteria criteria) {
		if (ObjectHelper.isNull(criteria) || TextHelper.isDefaultValue(criteria.idempotencyKey())) {
			return List.of();
		}
		return repository.findByIdempotencyKey(criteria.idempotencyKey()).stream()
				.map(NotificationRepositoryAdapter::toEntity)
				.toList();
	}

	@Override
	public void create(final NotificationEntity entity) {
		final NotificationJpaEntity notification = new NotificationJpaEntity();
		notification.setId(entity.id());
		notification.setIdempotencyKey(entity.idempotencyKey());
		notification.setKind(entity.kind());
		notification.setUser(entity.user());
		notification.setChannel(entity.channel());
		notification.setStatus(entity.status());
		notification.setProviderMessageId(entity.providerMessageId());
		repository.saveAndFlush(notification);
	}

	private static NotificationEntity toEntity(final NotificationJpaEntity notification) {
		return new NotificationEntity(
				notification.getId(),
				notification.getIdempotencyKey(),
				notification.getKind(),
				notification.getUser(),
				notification.getChannel(),
				notification.getStatus(),
				notification.getProviderMessageId());
	}

}
