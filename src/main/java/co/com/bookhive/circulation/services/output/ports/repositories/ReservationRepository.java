package co.com.bookhive.circulation.services.output.ports.repositories;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.criteria.ReservationCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;
import co.com.bookhive.shared.services.output.ports.repositories.CreateRepository;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;
import co.com.bookhive.shared.services.output.ports.repositories.FindRepository;

public interface ReservationRepository
		extends CreateRepository<ReservationEntity>,
		FindRepository<UUID, ReservationEntity>,
		FindByCriteriaRepository<ReservationEntity, ReservationCriteria> {

	boolean changeStatus(UUID reservation, String expectedStatus, String newStatus);

	boolean assign(UUID reservation, UUID bookCopy, Instant assignedAt, LocalDate pickupDeadline);

}
