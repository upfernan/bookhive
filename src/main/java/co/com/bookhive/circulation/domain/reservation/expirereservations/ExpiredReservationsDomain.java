package co.com.bookhive.circulation.domain.reservation.expirereservations;

public record ExpiredReservationsDomain(int expired, int handedOver) {

	public ExpiredReservationsDomain {
		expired = Math.max(expired, 0);
		handedOver = Math.max(handedOver, 0);
	}

}
