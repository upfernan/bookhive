package co.com.bookhive.circulation.services.usecase.lendbook.impl.mapper;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;

public interface LoanDomainMapper {

	LoanEntity toEntity(LendBookDomain domain);

}
