package co.com.bookhive.access.services.output.ports.repositories.entities;

import java.time.Instant;

import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record DataPolicyEntity(int version, String content, Instant publishedAt) {

	public DataPolicyEntity {
		content = TextHelper.getDefaultValue(content);
		publishedAt = DateHelper.getDefaultValue(publishedAt);
	}

}
