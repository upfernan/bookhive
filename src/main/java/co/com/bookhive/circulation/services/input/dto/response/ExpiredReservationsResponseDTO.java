package co.com.bookhive.circulation.services.input.dto.response;

public record ExpiredReservationsResponseDTO(int expired, int handedOver) {

	public ExpiredReservationsResponseDTO {
		expired = Math.max(expired, 0);
		handedOver = Math.max(handedOver, 0);
	}

}
