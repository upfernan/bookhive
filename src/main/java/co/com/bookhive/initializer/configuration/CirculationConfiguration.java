package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.circulation.services.input.adapters.CountBookCopiesInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.FindUserInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.LendBookInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.mapper.FindUserMapperImpl;
import co.com.bookhive.circulation.services.input.adapters.mapper.LendBookMapperImpl;
import co.com.bookhive.circulation.services.input.ports.CountBookCopiesInputPort;
import co.com.bookhive.circulation.services.input.ports.FindUserInputPort;
import co.com.bookhive.circulation.services.input.ports.LendBookInputPort;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.usecase.bookcopy.countbookcopies.CountBookCopiesUseCase;
import co.com.bookhive.circulation.services.usecase.bookcopy.countbookcopies.impl.CountBookCopiesUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.LendBookUseCase;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.impl.LendBookUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.loan.lendbook.impl.mapper.LoanDomainMapperImpl;
import co.com.bookhive.circulation.services.usecase.user.finduser.FindUserUseCase;
import co.com.bookhive.circulation.services.usecase.user.finduser.impl.FindUserUseCaseImpl;
import co.com.bookhive.shared.services.output.ports.AuditPort;

@Configuration
public class CirculationConfiguration {

	@Bean
	LendBookUseCase lendBookUseCase(
			final InstitutionParametersRepository institutionParametersRepository,
			final BookCopyRepository bookCopyRepository,
			final FineRepository fineRepository,
			final LoanRepository loanRepository,
			final SessionContextPort sessionContext,
			final AuditPort audit) {

		return new LendBookUseCaseImpl(
				institutionParametersRepository,
				bookCopyRepository,
				fineRepository,
				loanRepository,
				new LoanDomainMapperImpl(),
				sessionContext,
				audit);
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

}
