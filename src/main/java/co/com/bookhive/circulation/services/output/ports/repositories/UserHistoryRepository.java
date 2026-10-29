package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.List;
import java.util.UUID;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReturnedLoanEntity;

public interface UserHistoryRepository {

	List<ReturnedLoanEntity> findReturnedLoans(UUID user, int limit);

	List<FinePaymentEntity> findPayments(UUID user, int limit);

}
