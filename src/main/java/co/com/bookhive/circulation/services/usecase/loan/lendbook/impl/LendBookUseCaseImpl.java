package co.com.bookhive.circulation.services.usecase.loan.lendbook.impl;

import java.time.LocalDate;

import co.com.bookhive.circulation.domain.loan.lendbook.LendBookDomain;
import co.com.bookhive.circulation.services.output.ports.NotificationRequestPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.LendBookUseCase;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.impl.mapper.LoanDomainMapper;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class LendBookUseCaseImpl implements LendBookUseCase {

	private final InstitutionParametersRepository institutionParametersRepository;
	private final BookCopyRepository bookCopyRepository;
	private final FineRepository fineRepository;
	private final LoanRepository loanRepository;
	private final LoanDomainMapper loanDomainMapper;
	private final SessionContextPort sessionContext;
	private final AuditPort audit;
	private final NotificationRequestPort notificationRequests;

	@SuppressWarnings("java:S107")
	public LendBookUseCaseImpl(
			final InstitutionParametersRepository institutionParametersRepository,
			final BookCopyRepository bookCopyRepository,
			final FineRepository fineRepository,
			final LoanRepository loanRepository,
			final LoanDomainMapper loanDomainMapper,
			final SessionContextPort sessionContext,
			final AuditPort audit,
			final NotificationRequestPort notificationRequests) {

		super();
		this.institutionParametersRepository = requireDependency(
				institutionParametersRepository, "el repositorio de parámetros de la institución");
		this.bookCopyRepository = requireDependency(bookCopyRepository, "el repositorio de ejemplares");
		this.fineRepository = requireDependency(fineRepository, "el repositorio de multas");
		this.loanRepository = requireDependency(loanRepository, "el repositorio de préstamos");
		this.loanDomainMapper = requireDependency(loanDomainMapper, "el mapeador del préstamo");
		this.sessionContext = requireDependency(sessionContext, "el contexto de la sesión");
		this.audit = requireDependency(audit, "la auditoría");
		this.notificationRequests = requireDependency(notificationRequests, "la cola de avisos");
	}

	@Override
	public LendBookDomain execute(final LendBookDomain domain) {

		final LendBookDomain loan = withSession(requireLoanData(domain)).withRepositories(
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

		loanRepository.create(loanDomainMapper.toEntity(registeredLoan));

		final BookCopyEntity lentCopy = markBookCopyAsLent(registeredLoan);

		audit.record(
				AuditPort.LOAN_REGISTERED,
				registeredLoan.getInstitution(),
				registeredLoan.getLibrarian(),
				String.valueOf(registeredLoan.getId()));

		notificationRequests.request(new NotificationRequestEntity(
				LendBookDomain.NOTICE_KIND,
				registeredLoan.getUser(),
				lentCopy.getBook(),
				registeredLoan.getId(),
				registeredLoan.getDueDate()));

		return registeredLoan;
	}

	private LendBookDomain withSession(final LendBookDomain domain) {

		return new LendBookDomain(
				domain.getId(),
				domain.getBookCopy(),
				domain.getUser(),
				sessionContext.getInstitution(),
				sessionContext.getLibrarian(),
				domain.getLoanDate(),
				domain.getDueDate());
	}

	private BookCopyEntity markBookCopyAsLent(final LendBookDomain loan) {

		final BookCopyEntity bookCopy = bookCopyRepository.findById(loan.getBookCopy())
				.orElseGet(BookCopyEntity::new);

		bookCopy.setStatus(BookCopyEntity.STATUS_LENT);
		bookCopyRepository.update(bookCopy);
		return bookCopy;
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
