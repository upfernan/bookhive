package co.com.bookhive.circulation.domain.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;

class ReservationQueueTest {

	private static final UUID BOOK = UUID.randomUUID();
	private static final Instant MORNING = Instant.parse("2026-10-20T10:00:00Z");

	@Test
	@DisplayName("el puesto sale del orden de llegada, sin importar cómo vengan los datos")
	void positionFollowsArrivalOrder() {
		final ReservationEntity first = waitingAt(MORNING);
		final ReservationEntity second = waitingAt(MORNING.plusSeconds(30));
		final ReservationEntity third = waitingAt(MORNING.plusSeconds(90));

		final List<ReservationEntity> unordered = List.of(third, first, second);

		assertEquals(1, ReservationQueue.positionOf(first.getId(), unordered));
		assertEquals(2, ReservationQueue.positionOf(second.getId(), unordered));
		assertEquals(3, ReservationQueue.positionOf(third.getId(), unordered));
	}

	@Test
	@DisplayName("con la misma hora desempata el identificador, igual que la base de datos")
	void sameTimeIsBrokenById() {
		final ReservationEntity one = waitingAt(MORNING);
		final ReservationEntity other = waitingAt(MORNING);
		final ReservationEntity lower = one.getId().compareTo(other.getId()) < 0 ? one : other;
		final ReservationEntity higher = lower == one ? other : one;

		assertEquals(1, ReservationQueue.positionOf(lower.getId(), List.of(higher, lower)));
		assertEquals(2, ReservationQueue.positionOf(higher.getId(), List.of(higher, lower)));
	}

	@Test
	@DisplayName("las reservas que no están en espera no ocupan puesto y una ausente queda en cero")
	void onlyWaitingReservationsCount() {
		final ReservationEntity assigned =
				FakeRepositories.reservation(BOOK, UUID.randomUUID(), ReservationEntity.STATUS_ASSIGNED, MORNING);
		final ReservationEntity waiting = waitingAt(MORNING.plusSeconds(60));

		assertEquals(1, ReservationQueue.positionOf(waiting.getId(), List.of(assigned, waiting)));
		assertEquals(0, ReservationQueue.positionOf(assigned.getId(), List.of(assigned, waiting)));
		assertEquals(0, ReservationQueue.positionOf(UUID.randomUUID(), List.of(waiting)));
	}

	private static ReservationEntity waitingAt(final Instant requestedAt) {
		return FakeRepositories.reservation(BOOK, UUID.randomUUID(), ReservationEntity.STATUS_WAITING, requestedAt);
	}

}
