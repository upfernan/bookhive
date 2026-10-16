package co.com.bookhive.circulation.services.usecase.lendbook.impl;

import java.time.LocalDate;

import co.com.bookhive.circulation.domain.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.usecase.lendbook.LendBookUseCase;
import co.com.bookhive.circulation.services.usecase.lendbook.impl.mapper.LoanDomainMapper;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class LendBookUseCaseImpl implements LendBookUseCase {

	private final InstitutionParametersRepository institutionParametersRepository;
	private final BookCopyRepository bookCopyRepository;
	private final FineRepository fineRepository;
	private final LoanRepository loanRepository;

	public LendBookUseCaseImpl(
			final InstitutionParametersRepository institutionParametersRepository,
			final BookCopyRepository bookCopyRepository,
			final FineRepository fineRepository,
			final LoanRepository loanRepository) {

		super();
		this.institutionParametersRepository = requireDependency(
				institutionParametersRepository, "el repositorio de parámetros de la institución");
		this.bookCopyRepository = requireDependency(bookCopyRepository, "el repositorio de ejemplares");
		this.fineRepository = requireDependency(fineRepository, "el repositorio de multas");
		this.loanRepository = requireDependency(loanRepository, "el repositorio de préstamos");
	}

	@Override
	public LendBookDomain execute(final LendBookDomain domain) {

		final LendBookDomain loan = requireLoanData(domain).withRepositories(
				bookCopyRepository,
				fineRepository,
				loanRepository,
				institutionParametersRepository);

		loan.validate();

		final LocalDate dueDate = loan.calculateDueDate();

		final LendBookDomain registeredLoan = new LendBookDomain(
				UUIDHelper.generateNew(),
				loan.getBookCopy(),
				loan.getUser(),
				loan.getInstitution(),
				loan.getLibrarian(),
				loan.getLoanDate(),
				dueDate);

		loanRepository.create(LoanDomainMapper.toEntity(registeredLoan));

		markBookCopyAsLent(registeredLoan);

		return registeredLoan;
	}

	private void markBookCopyAsLent(final LendBookDomain loan) {

		final BookCopyEntity bookCopy = bookCopyRepository.findById(loan.getBookCopy())
				.orElseGet(BookCopyEntity::new);

		bookCopy.setStatus(BookCopyEntity.STATUS_LENT);
		bookCopyRepository.update(bookCopy);
	}

	private static LendBookDomain requireLoanData(final LendBookDomain domain) {

		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.create(
					MessageKey.LOAN_DATA_REQUIRED,
					"El caso de uso LendBook recibió un dominio nulo.");
		}

		return domain;
	}

	private static <T> T requireDependency(final T dependency, final String name) {

		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.LOAN_UNEXPECTED,
					"El caso de uso LendBook se creó sin " + name + ".");
		}

		return dependency;
	}

}
