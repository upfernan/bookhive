package co.com.bookhive.circulation.services.usecase.loan.reminddueloans.impl;

import java.util.List;

import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.domain.loan.reminddueloans.RemindDueLoansDomain;
import co.com.bookhive.circulation.services.output.ports.NotificationRequestPort;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.LoanCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;
import co.com.bookhive.circulation.services.usecase.loan.reminddueloans.RemindDueLoansUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class RemindDueLoansUseCaseImpl implements RemindDueLoansUseCase {

	private final InstitutionParametersRepository institutionParametersRepository;
	private final LoanRepository loanRepository;
	private final NotificationRequestPort notificationRequests;

	public RemindDueLoansUseCaseImpl(
			final InstitutionParametersRepository institutionParametersRepository,
			final LoanRepository loanRepository,
			final NotificationRequestPort notificationRequests) {

		super();
		this.institutionParametersRepository = requireDependency(
				institutionParametersRepository, "el repositorio de parámetros de la institución");
		this.loanRepository = requireDependency(loanRepository, "el repositorio de préstamos");
		this.notificationRequests = requireDependency(notificationRequests, "la cola de avisos");
	}

	@Override
	public RemindDueLoansDomain execute(final RemindDueLoansDomain domain) {
		final RemindDueLoansDomain reminder = ObjectHelper.getDefaultValue(domain, new RemindDueLoansDomain(0, null, 0))
				.from(CurrentInstitution.today(CurrentInstitution.parameters(institutionParametersRepository)));
		reminder.validate();

		final List<LoanEntity> loans = loanRepository.findByCriteria(LoanCriteria.activeDueOn(reminder.dueOn())).stream()
				.filter(RemindDueLoansUseCaseImpl::canBeReminded)
				.toList();

		loans.forEach(loan -> notificationRequests.request(new NotificationRequestEntity(
				RemindDueLoansDomain.NOTICE_KIND,
				loan.getUser().getId(),
				loan.getBookCopy().getBook(),
				loan.getId(),
				loan.getDueDate())));

		return reminder.withReminded(loans.size());
	}

	private static boolean canBeReminded(final LoanEntity loan) {
		return !UUIDHelper.isDefaultValue(loan.getId())
				&& !ObjectHelper.isNull(loan.getUser())
				&& !UUIDHelper.isDefaultValue(loan.getUser().getId())
				&& !ObjectHelper.isNull(loan.getBookCopy());
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.LOAN_UNEXPECTED,
					"El caso de uso RemindDueLoans se creó sin " + name + ".");
		}
		return dependency;
	}

}
