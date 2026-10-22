package co.com.bookhive.catalog.infrastructure.output.repositories.semistructured.typesense;

import co.com.bookhive.crosscutting.helper.TextHelper;

public record TypesenseResponse(int status, String body) {

	public TypesenseResponse {
		body = TextHelper.getDefaultValue(body);
	}

	public boolean isSuccess() {
		return status >= 200 && status < 300;
	}

}
