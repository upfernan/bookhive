package co.com.bookhive.circulation.domain.reservation;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;

public final class ReservationQueue {

	private static final Comparator<ReservationEntity> ARRIVAL_ORDER = Comparator
			.comparing(ReservationEntity::getRequestedAt)
			.thenComparing(ReservationEntity::getId);

	private ReservationQueue() {
		super();
	}

	public static int positionOf(final UUID reservation, final List<ReservationEntity> waiting) {
		final List<ReservationEntity> ordered = waiting.stream()
				.filter(ReservationEntity::isWaiting)
				.sorted(ARRIVAL_ORDER)
				.toList();

		for (int index = 0; index < ordered.size(); index++) {
			if (ordered.get(index).getId().equals(reservation)) {
				return index + 1;
			}
		}

		return 0;
	}

}
