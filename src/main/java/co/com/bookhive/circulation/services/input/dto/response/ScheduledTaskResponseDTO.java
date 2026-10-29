package co.com.bookhive.circulation.services.input.dto.response;

import java.util.List;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record ScheduledTaskResponseDTO(List<String> institutions) {

	public ScheduledTaskResponseDTO {
		institutions = List.copyOf(ObjectHelper.getDefaultValue(institutions, List.of()));
	}

}
