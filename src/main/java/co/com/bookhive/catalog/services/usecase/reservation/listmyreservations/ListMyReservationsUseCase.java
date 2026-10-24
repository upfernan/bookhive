package co.com.bookhive.catalog.services.usecase.reservation.listmyreservations;

import java.util.List;

import co.com.bookhive.catalog.domain.reservation.listmyreservations.MyReservationDomain;

public interface ListMyReservationsUseCase {

	List<MyReservationDomain> execute();

}
