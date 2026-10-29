package co.com.bookhive.access.domain.datapolicy.finddatapolicy;

import java.time.Instant;
import co.com.bookhive.crosscutting.helper.DateHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;

public record DataPolicyDomain(int version, String content, Instant publishedAt, Instant acceptedAt) {

	public DataPolicyDomain {
		content = TextHelper.getDefaultValue(content);
		publishedAt = DateHelper.getDefaultValue(publishedAt);
		acceptedAt = DateHelper.getDefaultValue(acceptedAt);
	}

	public boolean accepted() {
		return !DateHelper.isDefaultValue(acceptedAt);
	}

}
