package co.com.bookhive.catalog.infrastructure.output.externalservices.circulation;

import java.util.List;

import org.springframework.stereotype.Component;

import co.com.bookhive.catalog.services.output.ports.UserReservationsPort;
import co.com.bookhive.catalog.services.output.ports.entities.UserReservationEntity;
import co.com.bookhive.circulation.services.input.ports.FindUserReservationsInputPort;

@Component
public class CirculationUserReservationsAdapter implements UserReservationsPort {

	private final FindUserReservationsInputPort findUserReservations;

	public CirculationUserReservationsAdapter(final FindUserReservationsInputPort findUserReservations) {
		super();
		this.findUserReservations = findUserReservations;
	}

	@Override
	public List<UserReservationEntity> findMine() {
		return findUserReservations.execute().stream()
				.map(found -> new UserReservationEntity(
						found.reservation(),
						found.book(),
						found.status(),
						found.position(),
						found.requestedAt(),
						found.pickupDeadline()))
				.toList();
	}

}
