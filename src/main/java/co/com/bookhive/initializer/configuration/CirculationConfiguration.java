package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LoanRepository;
import co.com.bookhive.circulation.services.usecase.lendbook.LendBookUseCase;
import co.com.bookhive.circulation.services.usecase.lendbook.impl.LendBookUseCaseImpl;

@Configuration
public class CirculationConfiguration {

	@Bean
	LendBookUseCase lendBookUseCase(
			final InstitutionParametersRepository institutionParametersRepository,
			final BookCopyRepository bookCopyRepository,
			final FineRepository fineRepository,
			final LoanRepository loanRepository) {

		return new LendBookUseCaseImpl(
				institutionParametersRepository,
				bookCopyRepository,
				fineRepository,
				loanRepository);
	}

}
