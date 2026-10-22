package co.com.bookhive.catalog.services.output.ports.repositories.entities;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record FacetCountEntity(String value, long count) {

	public FacetCountEntity {
		value = TextHelper.getDefaultValue(value);
		count = Math.max(count, 0);
	}

}
