package co.com.bookhive.circulation.services.input.dto.request;

public record RemindDueLoansRequestDTO(int daysBefore) {

	public static final String TASK = "recordar-vencimientos";

	public RemindDueLoansRequestDTO {
		daysBefore = Math.max(daysBefore, 0);
	}

}
