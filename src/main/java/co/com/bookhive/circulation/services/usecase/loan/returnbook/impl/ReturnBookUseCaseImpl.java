package co.com.bookhive.circulation.services.usecase.loan.returnbook.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import co.com.bookhive.circulation.domain.loan.returnbook.FinePolicy;
import co.com.bookhive.circulation.domain.loan.returnbook.LoanState;
import co.com.bookhive.circulation.domain.loan.returnbook.ReturnBookDomain;
import co.com.bookhive.circulation.domain.loan.returnbook.ReturnedLoanDomain;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandOverBookCopyDomain;
import co.com.bookhive.circulation.domain.reservation.handoverbookcopy.HandedOverBookCopyDomain;
import co.com.bookhive.circulation.services.output.ports.NotificationRequestPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanReturnRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanReturnCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanReturnEntity;
import co.com.bookhive.circulation.services.usecase.loan.returnbook.ReturnBookUseCase;
import co.com.bookhive.circulation.services.usecase.reservation.handoverbookcopy.HandOverBookCopyUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class ReturnBookUseCaseImpl implements ReturnBookUseCase {

	private final InstitutionParametersRepository institutionParametersRepository;
	private final BookCopyRepository bookCopyRepository;
	private final LoanRepository loanRepository;
	private final LoanReturnRepository loanReturnRepository;
	private final FineRepository fineRepository;
	private final SessionContextPort sessionContext;
	private final AuditPort audit;
	private final NotificationRequestPort notificationRequests;
	private final HandOverBookCopyUseCase handOverBookCopy;

	@SuppressWarnings("java:S107")
	public ReturnBookUseCaseImpl(
			final InstitutionParametersRepository institutionParametersRepository,
			final BookCopyRepository bookCopyRepository,
			final LoanRepository loanRepository,
			final LoanReturnRepository loanReturnRepository,
			final FineRepository fineRepository,
			final SessionContextPort sessionContext,
			final AuditPort audit,
			final NotificationRequestPort notificationRequests,
			final HandOverBookCopyUseCase handOverBookCopy) {

		super();
		this.institutionParametersRepository = requireDependency(
				institutionParametersRepository, "el repositorio de parámetros de la institución");
		this.bookCopyRepository = requireDependency(bookCopyRepository, "el repositorio de ejemplares");
		this.loanRepository = requireDependency(loanRepository, "el repositorio de préstamos");
		this.loanReturnRepository = requireDependency(loanReturnRepository, "el repositorio de devoluciones");
		this.fineRepository = requireDependency(fineRepository, "el repositorio de multas");
		this.sessionContext = requireDependency(sessionContext, "el contexto de la sesión");
		this.audit = requireDependency(audit, "la auditoría");
		this.notificationRequests = requireDependency(notificationRequests, "la cola de avisos");
		this.handOverBookCopy = requireDependency(handOverBookCopy, "la entrega del ejemplar a la fila de reservas");
	}

	@Override
	public ReturnedLoanDomain execute(final ReturnBookDomain domain) {

		final ReturnBookDomain withPorts = requireReturnData(domain)
				.withSession(sessionContext.getInstitution(), sessionContext.getLibrarian())
				.withRepositories(bookCopyRepository, loanRepository, institutionParametersRepository);
		final ReturnBookDomain returning = withPorts.withReturnDate(withPorts.today());

		returning.validate();

		final Optional<LoanEntity> openLoan = returning.findOpenLoan();
		if (openLoan.isEmpty()) {
			return alreadyReturnedToday(returning).orElseThrow(returning::notOpen);
		}

		final LoanEntity loan = openLoan.get();
		LoanState.of(loan, false, returning.getReturnDate()).returnBook();

		final BookCopyEntity bookCopy = bookCopyRepository.findById(returning.getBookCopy())
				.orElseGet(BookCopyEntity::new);

		if (!bookCopyRepository.changeStatus(
				returning.getBookCopy(), BookCopyEntity.STATUS_LENT, BookCopyEntity.STATUS_AVAILABLE)) {
			return alreadyReturnedToday(returning).orElseThrow(returning::notOpen);
		}

		final int overdueDays = returning.overdueDaysFor(loan.getDueDate());
		loanReturnRepository.create(loanReturnOf(loan, returning, overdueDays));

		final FineEntity fine = overdueDays > 0
				? registerFine(loan, returning, overdueDays, returning.finePolicy())
				: new FineEntity();

		audit.register(
				AuditPort.LOAN_RETURNED,
				returning.getInstitution(),
				returning.getLibrarian(),
				auditDetailOf(loan, fine));

		if (overdueDays > 0) {
			notificationRequests.request(new NotificationRequestEntity(
					ReturnBookDomain.NOTICE_KIND,
					loan.getUser().getId(),
					bookCopy.getBook(),
					loan.getId(),
					loan.getDueDate()));
		}

		final HandedOverBookCopyDomain handedOver = handOverBookCopy.execute(
				new HandOverBookCopyDomain(returning.getBookCopy(), bookCopy.getBook()));

		return resultOf(loan, returning.getBookCopy(), returning.getReturnDate(), overdueDays, fine)
				.withReservedFor(handedOver.userName());
	}

	private Optional<ReturnedLoanDomain> alreadyReturnedToday(final ReturnBookDomain returning) {
		return loanReturnRepository.findByCriteria(
				LoanReturnCriteria.ofBookCopyOn(returning.getBookCopy(), returning.getReturnDate()))
				.stream()
				.findFirst()
				.flatMap(loanReturn -> resultOf(loanReturn, returning.getBookCopy()));
	}

	private Optional<ReturnedLoanDomain> resultOf(final LoanReturnEntity loanReturn, final UUID bookCopy) {
		return loanRepository.findById(loanReturn.getLoan())
				.map(loan -> resultOf(
						loan,
						bookCopy,
						loanReturn.getReturnDate(),
						loanReturn.getOverdueDays(),
						fineRepository.findByCriteria(FineCriteria.ofLoan(loan.getId())).stream()
								.findFirst()
								.orElseGet(FineEntity::new)));
	}

	private static ReturnedLoanDomain resultOf(
			final LoanEntity loan,
			final UUID bookCopy,
			final LocalDate returnDate,
			final int overdueDays,
			final FineEntity fine) {

		final boolean fined = !UUIDHelper.isDefaultValue(fine.getId());
		return new ReturnedLoanDomain(
				loan.getId(),
				bookCopy,
				loan.getDueDate(),
				returnDate,
				overdueDays,
				fine.getId(),
				fined ? fine.getDailyRate() : BigDecimal.ZERO,
				fined ? fine.getTotalAmount() : BigDecimal.ZERO);
	}

	private static LoanReturnEntity loanReturnOf(
			final LoanEntity loan, final ReturnBookDomain returning, final int overdueDays) {

		final LoanReturnEntity loanReturn = new LoanReturnEntity();
		loanReturn.setId(UUIDHelper.generateNew());
		loanReturn.setLoan(loan.getId());
		loanReturn.setReturnDate(returning.getReturnDate());
		loanReturn.setOverdueDays(overdueDays);
		loanReturn.setLibrarian(returning.getLibrarian());
		return loanReturn;
	}

	private FineEntity registerFine(
			final LoanEntity loan,
			final ReturnBookDomain returning,
			final int overdueDays,
			final FinePolicy finePolicy) {

		final FineEntity fine = new FineEntity();
		fine.setId(UUIDHelper.generateNew());
		fine.setLoan(loan);
		fine.setUser(loan.getUser());
		fine.setIssueDate(returning.getReturnDate());
		fine.setOverdueDays(overdueDays);
		fine.setDailyRate(finePolicy.dailyRate());
		fine.setTotalAmount(finePolicy.amountFor(overdueDays));
		fine.setStatus(FineEntity.STATUS_PENDING);
		fineRepository.create(fine);
		return fine;
	}

	private static String auditDetailOf(final LoanEntity loan, final FineEntity fine) {
		final UUID fineId = fine.getId();
		return UUIDHelper.isDefaultValue(fineId)
				? String.valueOf(loan.getId())
				: loan.getId() + " multa " + fineId;
	}

	private static ReturnBookDomain requireReturnData(final ReturnBookDomain domain) {
		if (ObjectHelper.isNull(domain)) {
			throw BookHiveException.create(
					MessageKey.LOAN_DATA_REQUIRED,
					"El caso de uso ReturnBook recibió un dominio nulo.");
		}
		return domain;
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.LOAN_UNEXPECTED,
					"El caso de uso ReturnBook se creó sin " + name + ".");
		}
		return dependency;
	}

}
