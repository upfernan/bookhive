package co.com.bookhive.circulation.services.usecase.account.finduseraccount.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import co.com.bookhive.circulation.domain.account.finduseraccount.UserAccountDomain;
import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.loan.returnbook.DailyRateFinePolicy;
import co.com.bookhive.circulation.domain.loan.returnbook.FinePolicy;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserHistoryRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;
import co.com.bookhive.circulation.services.usecase.account.finduseraccount.FindUserAccountUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindUserAccountUseCaseImpl implements FindUserAccountUseCase {

	public static final int HISTORY_LIMIT = 50;

	private final SessionContextPort sessionContext;
	private final InstitutionParametersRepository institutionParameters;
	private final LoanRepository loans;
	private final FineRepository fines;
	private final UserHistoryRepository history;

	public FindUserAccountUseCaseImpl(
			final SessionContextPort sessionContext,
			final InstitutionParametersRepository institutionParameters,
			final LoanRepository loans,
			final FineRepository fines,
			final UserHistoryRepository history) {

		super();
		this.sessionContext = requireDependency(sessionContext, "el contexto de la sesión");
		this.institutionParameters = requireDependency(institutionParameters, "los parámetros de la institución");
		this.loans = requireDependency(loans, "el repositorio de préstamos");
		this.fines = requireDependency(fines, "el repositorio de multas");
		this.history = requireDependency(history, "el historial del usuario");
	}

	@Override
	public UserAccountDomain execute() {

		final UUID user = sessionContext.getLibrarian();
		if (UUIDHelper.isDefaultValue(user)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED, "FindUserAccount no recibió el usuario de la sesión.");
		}

		final InstitutionParametersEntity parameters = CurrentInstitution.parameters(institutionParameters);
		final LocalDate today = CurrentInstitution.today(parameters);
		final FinePolicy policy = new DailyRateFinePolicy(parameters.getDailyFineRate());

		final List<UserAccountDomain.ActiveLoan> activeLoans = loans.findByCriteria(LoanCriteria.activeByUser(user)).stream()
				.map(loan -> UserAccountDomain.ActiveLoan.on(loan.getId(), loan.getBookCopy().getBook(),
						loan.getLoanDate(), loan.getDueDate(), today, policy))
				.sorted(Comparator.comparing(UserAccountDomain.ActiveLoan::dueDate))
				.toList();

		final List<UserAccountDomain.PendingFine> pendingFines = fines.findByCriteria(FineCriteria.pendingByUser(user)).stream()
				.map(fine -> new UserAccountDomain.PendingFine(fine.getId(), fine.getLoan().getBookCopy().getBook(),
						fine.getIssueDate(), fine.getOverdueDays(), fine.getDailyRate(), fine.getTotalAmount()))
				.sorted(Comparator.comparing(UserAccountDomain.PendingFine::issueDate))
				.toList();

		final List<UserAccountDomain.ReturnedLoan> returnedLoans = history.findReturnedLoans(user, HISTORY_LIMIT).stream()
				.map(row -> new UserAccountDomain.ReturnedLoan(
						row.loan(), row.book(), row.loanDate(), row.dueDate(), row.returnDate(), row.overdueDays()))
				.toList();

		final List<UserAccountDomain.Payment> payments = history.findPayments(user, HISTORY_LIMIT).stream()
				.map(row -> new UserAccountDomain.Payment(row.fine(), row.book(), row.reference(), row.amount(), row.paidAt()))
				.toList();

		return new UserAccountDomain(activeLoans, pendingFines, returnedLoans, payments);
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED, "El caso de uso FindUserAccount se creó sin " + name + ".");
		}
		return dependency;
	}

}
