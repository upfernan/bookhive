package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.UserHistoryJpaRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserHistoryRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReturnedLoanEntity;

@Component
public class UserHistoryRepositoryAdapter implements UserHistoryRepository {

	private final UserHistoryJpaRepository repository;

	public UserHistoryRepositoryAdapter(final UserHistoryJpaRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public List<ReturnedLoanEntity> findReturnedLoans(final UUID user, final int limit) {
		return repository.findReturnedLoans(user, PageRequest.of(0, Math.max(limit, 1)));
	}

	@Override
	public List<FinePaymentEntity> findPayments(final UUID user, final int limit) {
		return repository.findPayments(user, PageRequest.of(0, Math.max(limit, 1)));
	}

}
