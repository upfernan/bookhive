package co.com.bookhive.circulation.services.usecase.fine.settlefine.impl;

import co.com.bookhive.circulation.domain.fine.settlefine.SettleFineDomain;
import co.com.bookhive.circulation.domain.fine.settlefine.SettledFineDomain;
import co.com.bookhive.circulation.domain.institution.CurrentInstitution;
import co.com.bookhive.circulation.services.output.ports.NotificationRequestPort;
import co.com.bookhive.circulation.services.output.ports.entities.NotificationRequestEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.FineSettlementRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.InstitutionParametersRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineSettlementCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineSettlementEntity;
import co.com.bookhive.circulation.services.usecase.fine.settlefine.SettleFineUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class SettleFineUseCaseImpl implements SettleFineUseCase {

	private final FineRepository fineRepository;
	private final FineSettlementRepository fineSettlementRepository;
	private final BookCopyRepository bookCopyRepository;
	private final InstitutionParametersRepository institutionParametersRepository;
	private final NotificationRequestPort notificationRequests;

	public SettleFineUseCaseImpl(
			final FineRepository fineRepository,
			final FineSettlementRepository fineSettlementRepository,
			final BookCopyRepository bookCopyRepository,
			final InstitutionParametersRepository institutionParametersRepository,
			final NotificationRequestPort notificationRequests) {

		super();
		this.fineRepository = requireDependency(fineRepository, "el repositorio de multas");
		this.fineSettlementRepository = requireDependency(fineSettlementRepository, "el repositorio de multas saldadas");
		this.bookCopyRepository = requireDependency(bookCopyRepository, "el repositorio de ejemplares");
		this.institutionParametersRepository = requireDependency(
				institutionParametersRepository, "el repositorio de parámetros de la institución");
		this.notificationRequests = requireDependency(notificationRequests, "la cola de avisos");
	}

	@Override
	public SettledFineDomain execute(final SettleFineDomain domain) {
		final SettleFineDomain request = ObjectHelper.getDefaultValue(domain, new SettleFineDomain(null, null, null));
		request.validate();

		final FineEntity fine = fineRepository.findByCriteria(FineCriteria.withId(request.fine())).stream()
				.findFirst()
				.orElseThrow(() -> BookHiveException.createUnexpected(
						MessageKey.FINE_UNEXPECTED,
						"El pago " + request.paymentReference() + " es de la multa " + request.fine() + ", que no existe."));

		if (!request.coversExactly(fine.getTotalAmount())) {
			throw BookHiveException.createConflict(
					MessageKey.FINE_UNEXPECTED,
					"El pago " + request.paymentReference() + " trae " + request.amount()
							+ " y la multa " + fine.getId() + " vale " + fine.getTotalAmount() + ".");
		}

		final FineSettlementEntity settlement = new FineSettlementEntity(
				UUIDHelper.generateNew(), fine.getId(), request.paymentReference(), request.amount(), null);

		if (!fineSettlementRepository.settle(settlement)) {
			final String settledBy = fineSettlementRepository.findByCriteria(FineSettlementCriteria.ofFine(fine.getId()))
					.stream()
					.findFirst()
					.map(FineSettlementEntity::paymentReference)
					.orElse(request.paymentReference());
			return new SettledFineDomain(false, settledBy);
		}

		notificationRequests.request(new NotificationRequestEntity(
				SettleFineDomain.NOTICE_KIND,
				fine.getUser().getId(),
				bookCopyRepository.findById(fine.getLoan().getBookCopy().getId())
						.map(BookCopyEntity::getBook)
						.orElseGet(UUIDHelper::getDefaultValue),
				fine.getLoan().getId(),
				CurrentInstitution.today(CurrentInstitution.parameters(institutionParametersRepository))));

		return new SettledFineDomain(true, request.paymentReference());
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.FINE_UNEXPECTED, "El caso de uso SettleFine se creó sin " + name + ".");
		}
		return dependency;
	}

}
