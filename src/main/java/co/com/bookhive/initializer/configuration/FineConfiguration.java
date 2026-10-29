package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.com.bookhive.circulation.services.input.adapters.FindPayableFineInputAdapter;
import co.com.bookhive.circulation.services.input.adapters.SettleFineInputAdapter;
import co.com.bookhive.circulation.services.input.ports.FindPayableFineInputPort;
import co.com.bookhive.circulation.services.input.ports.SettleFineInputPort;
import co.com.bookhive.circulation.services.output.ports.NotificationRequestPort;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineSettlementRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.usecase.fine.findpayablefine.FindPayableFineUseCase;
import co.com.bookhive.circulation.services.usecase.fine.findpayablefine.impl.FindPayableFineUseCaseImpl;
import co.com.bookhive.circulation.services.usecase.fine.settlefine.SettleFineUseCase;
import co.com.bookhive.circulation.services.usecase.fine.settlefine.impl.SettleFineUseCaseImpl;

@Configuration
public class FineConfiguration {

	@Bean
	FindPayableFineUseCase findPayableFineUseCase(final FineRepository fineRepository) {
		return new FindPayableFineUseCaseImpl(fineRepository);
	}

	@Bean
	FindPayableFineInputPort findPayableFineInputPort(final FindPayableFineUseCase findPayableFineUseCase) {
		return new FindPayableFineInputAdapter(findPayableFineUseCase);
	}

	@Bean
	SettleFineUseCase settleFineUseCase(
			final FineRepository fineRepository,
			final FineSettlementRepository fineSettlementRepository,
			final BookCopyRepository bookCopyRepository,
			final InstitutionParametersRepository institutionParametersRepository,
			final NotificationRequestPort notificationRequests) {

		return new SettleFineUseCaseImpl(
				fineRepository,
				fineSettlementRepository,
				bookCopyRepository,
				institutionParametersRepository,
				notificationRequests);
	}

	@Bean
	SettleFineInputPort settleFineInputPort(final SettleFineUseCase settleFineUseCase) {
		return new SettleFineInputAdapter(settleFineUseCase);
	}

}
