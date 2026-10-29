package co.com.bookhive.circulation.services.usecase.reservation.finduserreservations.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.reservation.finduserreservations.UserReservationDomain;
import co.com.bookhive.circulation.services.output.ports.SessionContextPort;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;
import co.com.bookhive.crosscutting.exception.BookHiveException;

class FindUserReservationsUseCaseImplTest {

	private static final UUID READER = UUID.randomUUID();
	private static final UUID WAITED_BOOK = UUID.randomUUID();
	private static final UUID ASSIGNED_BOOK = UUID.randomUUID();
	private static final Instant MORNING = Instant.parse("2026-10-20T10:00:00Z");

	@Test
	@DisplayName("lista solo las reservas activas del usuario con su puesto en la fila")
	void listsActiveReservationsWithPosition() {
		final ReservationEntity mine =
				FakeRepositories.reservation(WAITED_BOOK, READER, ReservationEntity.STATUS_WAITING, MORNING.plusSeconds(60));
		final ReservationEntity assigned =
				FakeRepositories.reservation(ASSIGNED_BOOK, READER, ReservationEntity.STATUS_ASSIGNED, MORNING);
		assigned.setPickupDeadline(LocalDate.of(2026, 10, 26));
		final FakeRepositories.Reservations reservations = new FakeRepositories.Reservations()
				.with(FakeRepositories.reservation(WAITED_BOOK, UUID.randomUUID(), ReservationEntity.STATUS_WAITING, MORNING))
				.with(mine)
				.with(assigned)
				.with(FakeRepositories.reservation(WAITED_BOOK, READER, ReservationEntity.STATUS_CANCELLED, MORNING));

		final List<UserReservationDomain> found = useCase(reservations, READER).execute();

		assertEquals(2, found.size());
		final UserReservationDomain waiting = found.get(0);
		assertEquals(mine.getId(), waiting.reservation());
		assertEquals(WAITED_BOOK, waiting.book());
		assertEquals(ReservationEntity.STATUS_WAITING, waiting.status());
		assertEquals(2, waiting.position());
		assertEquals(MORNING.plusSeconds(60), waiting.requestedAt());
		final UserReservationDomain ready = found.get(1);
		assertEquals(ReservationEntity.STATUS_ASSIGNED, ready.status());
		assertEquals(0, ready.position());
		assertEquals(LocalDate.of(2026, 10, 26), ready.pickupDeadline());
	}

	@Test
	@DisplayName("solo consulta la fila de las reservas que están en espera")
	void readsQueueOnlyForWaitingReservations() {
		final FakeRepositories.Reservations reservations = new FakeRepositories.Reservations()
				.with(FakeRepositories.reservation(ASSIGNED_BOOK, READER, ReservationEntity.STATUS_ASSIGNED, MORNING));

		useCase(reservations, READER).execute();

		assertEquals(1, reservations.searches().size());
	}

	@Test
	@DisplayName("sin usuario en la sesión o sin dependencias se rechaza")
	void rejectsMissingUserAndDependencies() {
		final FakeRepositories.Reservations reservations = new FakeRepositories.Reservations();
		final FindUserReservationsUseCaseImpl withoutUser = useCase(reservations, null);

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, withoutUser::execute).getKind());
		assertThrows(BookHiveException.class, () -> new FindUserReservationsUseCaseImpl(reservations, null));
	}

	private static FindUserReservationsUseCaseImpl useCase(
			final FakeRepositories.Reservations reservations, final UUID user) {

		return new FindUserReservationsUseCaseImpl(reservations, new SessionContextPort() {

			@Override
			public UUID getInstitution() {
				return UUID.randomUUID();
			}

			@Override
			public UUID getLibrarian() {
				return user;
			}

		});
	}

}
