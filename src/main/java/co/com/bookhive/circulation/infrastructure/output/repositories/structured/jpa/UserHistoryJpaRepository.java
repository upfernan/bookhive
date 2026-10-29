package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FinePaymentEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReturnedLoanEntity;

public interface UserHistoryJpaRepository extends Repository<LoanJpaEntity, UUID> {

	@Query("SELECT new co.com.bookhive.circulation.services.output.ports.repositories.entities.ReturnedLoanEntity("
			+ "l.id, l.bookCopy.book, l.loanDate, l.dueDate, r.returnDate, r.overdueDays)"
			+ " FROM LoanJpaEntity l JOIN LoanReturnJpaEntity r ON r.loan = l.id"
			+ " WHERE l.user.id = :user ORDER BY r.returnDate DESC, l.loanDate DESC")
	List<ReturnedLoanEntity> findReturnedLoans(@Param("user") UUID user, Pageable page);

	@Query("SELECT new co.com.bookhive.circulation.services.output.ports.repositories.entities.FinePaymentEntity("
			+ "f.id, f.loan.bookCopy.book, s.paymentReference, s.amount, s.settledAt)"
			+ " FROM FineSettlementJpaEntity s JOIN FineJpaEntity f ON f.id = s.fine"
			+ " WHERE f.user.id = :user ORDER BY s.settledAt DESC")
	List<FinePaymentEntity> findPayments(@Param("user") UUID user, Pageable page);

}
