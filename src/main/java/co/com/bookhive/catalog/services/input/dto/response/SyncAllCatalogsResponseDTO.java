package co.com.bookhive.catalog.services.input.dto.response;

import java.util.List;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public record SyncAllCatalogsResponseDTO(List<String> synced, List<String> failed) {

	public SyncAllCatalogsResponseDTO {
		synced = List.copyOf(ObjectHelper.getDefaultValue(synced, List.of()));
		failed = List.copyOf(ObjectHelper.getDefaultValue(failed, List.of()));
	}

}
