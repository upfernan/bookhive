package co.com.bookhive.circulation.services.output.ports.repositories.criteria;

import java.util.UUID;
import co.com.bookhive.crosscutting.helper.UUIDHelper;

public final class FineSettlementCriteria {

	private final UUID fine;

	private FineSettlementCriteria(final UUID fine) {
		super();
		this.fine = UUIDHelper.getDefaultValue(fine);
	}

	public static FineSettlementCriteria ofFine(final UUID fine) {
		return new FineSettlementCriteria(fine);
	}

	public UUID getFine() {
		return fine;
	}

}
