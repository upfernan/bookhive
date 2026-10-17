package co.com.bookhive.circulation.services.usecase.lendbook.impl.mapper;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class LoanDomainMapperImpl implements LoanDomainMapper {

	@Override
	public LoanEntity toEntity(final LendBookDomain domain) {

		final LoanEntity entity = new LoanEntity();

		if (ObjectHelper.isNull(domain)) {
			return entity;
		}

		entity.setId(domain.getId());
		final BookCopyEntity bookCopy = new BookCopyEntity();
		bookCopy.setId(domain.getBookCopy());
		entity.setBookCopy(bookCopy);

		final UserEntity user = new UserEntity();
		user.setId(domain.getUser());
		entity.setUser(user);

		entity.setLibrarian(domain.getLibrarian());
		entity.setLoanDate(domain.getLoanDate());
		entity.setDueDate(domain.getDueDate());

		entity.setStatus(LoanEntity.STATUS_ACTIVE);

		return entity;
	}

}
