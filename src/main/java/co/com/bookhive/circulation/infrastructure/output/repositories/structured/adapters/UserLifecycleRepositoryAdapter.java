package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.UserLifecycleJpaRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserLifecycleRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserLifecycleEntity;

@Component
public class UserLifecycleRepositoryAdapter implements UserLifecycleRepository {

	private static final int DEFAULT_RETENTION = 24;

	private final UserLifecycleJpaRepository repository;

	public UserLifecycleRepositoryAdapter(final UserLifecycleJpaRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public Optional<UserLifecycleEntity> lock(final UUID user) {
		return repository.lock(user).stream().findFirst().map(row -> new UserLifecycleEntity(row.getId(), row.getStatus(),
				row.getDeactivatedAt()));
	}

	@Override
	public long activeLoans(final UUID user) {
		return repository.activeLoans(user);
	}

	@Override
	public long pendingFines(final UUID user) {
		return repository.pendingFines(user);
	}

	@Override
	public void changeStatus(final UUID user, final String status) {
		repository.changeStatus(user, status);
	}

	@Override
	public void anonymize(final UUID user) {
		repository.anonymize(user);
	}

	@Override
	public List<UUID> expiredInactive(final int months) {
		return repository.expiredInactive(months);
	}

	@Override
	public int retentionMonths() {
		return repository.retentionMonths().stream().findFirst().orElse(DEFAULT_RETENTION);
	}

	@Override
	public void changeRetention(final int months) {
		repository.changeRetention(months);
	}

}
