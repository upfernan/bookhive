package co.com.bookhive.catalog.services.output.ports;

import java.util.List;

import co.com.bookhive.catalog.services.output.ports.entities.UserReservationEntity;

public interface UserReservationsPort {

	List<UserReservationEntity> findMine();

}
