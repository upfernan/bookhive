package co.com.bookhive.circulation.services.usecase.fine.findpayablefine.impl;

import co.com.bookhive.circulation.domain.fine.findpayablefine.FindPayableFineDomain;
import co.com.bookhive.circulation.domain.fine.findpayablefine.PayableFineDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.FineRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.FineCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;
import co.com.bookhive.circulation.services.usecase.fine.findpayablefine.FindPayableFineUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class FindPayableFineUseCaseImpl implements FindPayableFineUseCase {

	private final FineRepository fineRepository;

	public FindPayableFineUseCaseImpl(final FineRepository fineRepository) {
		super();
		this.fineRepository = requireDependency(fineRepository);
	}

	@Override
	public PayableFineDomain execute(final FindPayableFineDomain domain) {
		final FindPayableFineDomain request = ObjectHelper.getDefaultValue(domain, new FindPayableFineDomain(null, null));
		request.validate();

		final FineEntity fine = fineRepository.findByCriteria(FineCriteria.ofUser(request.fine(), request.user()))
				.stream()
				.findFirst()
				.orElseThrow(request::notFound);

		final boolean pending = fineRepository.findByCriteria(FineCriteria.pendingByUser(request.user())).stream()
				.anyMatch(pendingFine -> fine.getId().equals(pendingFine.getId()));

		return new PayableFineDomain(fine.getId(), fine.getTotalAmount(), pending);
	}

	private static FineRepository requireDependency(final FineRepository fineRepository) {
		if (ObjectHelper.isNull(fineRepository)) {
			throw BookHiveException.createUnexpected(
					MessageKey.FINE_UNEXPECTED, "El caso de uso FindPayableFine se creó sin el repositorio de multas.");
		}
		return fineRepository;
	}

}
