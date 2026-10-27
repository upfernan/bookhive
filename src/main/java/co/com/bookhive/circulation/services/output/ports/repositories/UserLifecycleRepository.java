package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserLifecycleEntity;

public interface UserLifecycleRepository {

	Optional<UserLifecycleEntity> lock(UUID user);

	long activeLoans(UUID user);

	long pendingFines(UUID user);

	void changeStatus(UUID user, String status);

	void anonymize(UUID user);

	List<UUID> expiredInactive(int months);

	int retentionMonths();

	void changeRetention(int months);

}
