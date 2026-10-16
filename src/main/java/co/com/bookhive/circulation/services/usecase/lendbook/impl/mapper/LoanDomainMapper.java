package co.com.bookhive.circulation.services.usecase.lendbook.impl.mapper;

import co.com.bookhive.circulation.domain.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class LoanDomainMapper {

	private LoanDomainMapper() {
		super();
	}

	public static LoanEntity toEntity(final LendBookDomain domain) {

		final LoanEntity entity = new LoanEntity();

		if (ObjectHelper.isNull(domain)) {
			return entity;
		}

		entity.setId(domain.getId());
		entity.setBookCopy(domain.getBookCopy());
		entity.setUser(domain.getUser());
		entity.setLibrarian(domain.getLibrarian());
		entity.setLoanDate(domain.getLoanDate());
		entity.setDueDate(domain.getDueDate());

		entity.setStatus(LoanEntity.STATUS_ACTIVE);

		return entity;
	}

}
