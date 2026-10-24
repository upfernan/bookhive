package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.circulation.services.input.adapters.CountBookCopiesInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.FindBookCopyInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.FindUserContactInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.FindUserInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.LendBookInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.RemindDueLoansInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.ReturnBookInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.ScheduleDueRemindersInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.mapper.FindBookCopyMapperImpl;
import co.com.bookhive.circulation.services.input.adapters.mapper.FindUserMapperImpl;
import co.com.bookhive.circulation.services.input.adapters.mapper.LendBookMapperImpl;
import co.com.bookhive.circulation.services.input.ports.CountBookCopiesInputPort;
import co.com.bookhive.circulation.services.input.ports.FindBookCopyInputPort;
import co.com.bookhive.circulation.services.input.ports.FindUserContactInputPort;
import co.com.bookhive.circulation.services.input.ports.FindUserInputPort;
import co.com.bookhive.circulation.services.input.ports.LendBookInputPort;
import co.com.bookhive.circulation.services.input.ports.RemindDueLoansInputPort;
import co.com.bookhive.circulation.services.input.ports.ReturnBookInputPort;
import co.com.bookhive.circulation.services.input.ports.ScheduleDueRemindersInputPort;
import co.com.bookhive.circulation.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.circulation.services.output.ports.InstitutionTaskPort;
import co.com.bookhive.circulation.services.output.ports.NotificationRequestPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanReturnRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.usecase.bookcopy.countbookcopies.CountBookCopiesUseCase;
import co.com.bookhive.circulation.services.usecase.bookcopy.countbookcopies.impl.CountBookCopiesUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.bookcopy.findbookcopy.FindBookCopyUseCase;
import co.com.bookhive.circulation.services.usecase.bookcopy.findbookcopy.impl.FindBookCopyUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.LendBookUseCase;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.impl.LendBookUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.impl.mapper.LoanDomainMapperImpl;
import co.com.bookhive.circulation.services.usecase.loan.reminddueloans.RemindDueLoansUseCase;
import co.com.bookhive.circulation.services.usecase.loan.reminddueloans.impl.RemindDueLoansUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.loan.returnbook.ReturnBookUseCase;
import co.com.bookhive.circulation.services.usecase.loan.returnbook.impl.ReturnBookUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.loan.scheduleduereminders.ScheduleDueRemindersUseCase;
import co.com.bookhive.circulation.services.usecase.loan.scheduleduereminders.impl.ScheduleDueRemindersUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.user.finduser.FindUserUseCase;
import co.com.bookhive.circulation.services.usecase.user.finduser.impl.FindUserUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.user.findusercontact.FindUserContactUseCase;
import co.com.bookhive.circulation.services.usecase.user.findusercontact.impl.FindUserContactUseCaseImpl;
import co.com.bookhive.shared.services.output.ports.AuditPort;

@Configuration
public class CirculationConfiguration {

	@Bean
	LendBookUseCase lendBookUseCase(
			final InstitutionParametersRepository institutionParametersRepository,
			final BookCopyRepository bookCopyRepository,
			final FineRepository fineRepository,
			final LoanRepository loanRepository,
			final UserRepository userRepository,
			final SessionContextPort sessionContext,
			final AuditPort audit,
			final NotificationRequestPort notificationRequests) {

		return new LendBookUseCaseImpl(
				institutionParametersRepository,
				bookCopyRepository,
				fineRepository,
				loanRepository,
				userRepository,
				new LoanDomainMapperImpl(),
				sessionContext,
				audit,
				notificationRequests);
	}

	@Bean
	LendBookInputPort lendBookInputPort(final LendBookUseCase lendBookUseCase) {

		return new LendBookInputAdapter(lendBookUseCase, new LendBookMapperImpl());
	}

	@Bean
	CountBookCopiesUseCase countBookCopiesUseCase(final BookCopyRepository bookCopyRepository) {
		return new CountBookCopiesUseCaseImpl(bookCopyRepository);
	}

	@Bean
	CountBookCopiesInputPort countBookCopiesInputPort(final CountBookCopiesUseCase countBookCopiesUseCase) {
		return new CountBookCopiesInputAdapter(countBookCopiesUseCase);
	}

	@Bean
	FindUserUseCase findUserUseCase(
			final UserRepository userRepository,
			final FineRepository fineRepository,
			final LoanRepository loanRepository,
			final InstitutionParametersRepository institutionParametersRepository) {

		return new FindUserUseCaseImpl(
				userRepository,
				fineRepository,
				loanRepository,
				institutionParametersRepository);
	}

	@Bean
	FindUserInputPort findUserInputPort(final FindUserUseCase findUserUseCase) {
		return new FindUserInputAdapter(findUserUseCase, new FindUserMapperImpl());
	}

	@Bean
	FindBookCopyUseCase findBookCopyUseCase(final BookCopyRepository bookCopyRepository) {
		return new FindBookCopyUseCaseImpl(bookCopyRepository);
	}

	@Bean
	FindBookCopyInputPort findBookCopyInputPort(final FindBookCopyUseCase findBookCopyUseCase) {
		return new FindBookCopyInputAdapter(findBookCopyUseCase, new FindBookCopyMapperImpl());
	}

	@Bean
	FindUserContactUseCase findUserContactUseCase(final UserRepository userRepository) {
		return new FindUserContactUseCaseImpl(userRepository);
	}

	@Bean
	FindUserContactInputPort findUserContactInputPort(final FindUserContactUseCase findUserContactUseCase) {
		return new FindUserContactInputAdapter(findUserContactUseCase);
	}

	@Bean
	RemindDueLoansUseCase remindDueLoansUseCase(
			final InstitutionParametersRepository institutionParametersRepository,
			final LoanRepository loanRepository,
			final NotificationRequestPort notificationRequests) {

		return new RemindDueLoansUseCaseImpl(institutionParametersRepository, loanRepository, notificationRequests);
	}

	@Bean
	RemindDueLoansInputPort remindDueLoansInputPort(final RemindDueLoansUseCase remindDueLoansUseCase) {
		return new RemindDueLoansInputAdapter(remindDueLoansUseCase);
	}

	@Bean
	ScheduleDueRemindersUseCase scheduleDueRemindersUseCase(
			final InstitutionDirectoryPort institutionDirectory,
			final InstitutionTaskPort institutionTasks) {

		return new ScheduleDueRemindersUseCaseImpl(institutionDirectory, institutionTasks);
	}

	@Bean
	ScheduleDueRemindersInputPort scheduleDueRemindersInputPort(
			final ScheduleDueRemindersUseCase scheduleDueRemindersUseCase) {

		return new ScheduleDueRemindersInputAdapter(scheduleDueRemindersUseCase);
	}

	@Bean
	@SuppressWarnings("java:S107")
	ReturnBookUseCase returnBookUseCase(
			final InstitutionParametersRepository institutionParametersRepository,
			final BookCopyRepository bookCopyRepository,
			final LoanRepository loanRepository,
			final LoanReturnRepository loanReturnRepository,
			final FineRepository fineRepository,
			final SessionContextPort sessionContext,
			final AuditPort audit,
			final NotificationRequestPort notificationRequests) {

		return new ReturnBookUseCaseImpl(
				institutionParametersRepository,
				bookCopyRepository,
				loanRepository,
				loanReturnRepository,
				fineRepository,
				sessionContext,
				audit,
				notificationRequests);
	}

	@Bean
	ReturnBookInputPort returnBookInputPort(final ReturnBookUseCase returnBookUseCase) {
		return new ReturnBookInputAdapter(returnBookUseCase);
	}
}
