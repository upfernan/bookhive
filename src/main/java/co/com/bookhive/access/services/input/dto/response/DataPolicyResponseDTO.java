package co.com.bookhive.access.services.input.dto.response;

import java.time.Instant;

import co.com.bookhive.access.domain.datapolicy.finddatapolicy.DataPolicyDomain;

public record DataPolicyResponseDTO(int version, String content, Instant publishedAt, boolean accepted, Instant acceptedAt) {

	public static DataPolicyResponseDTO of(final DataPolicyDomain policy) {
		return new DataPolicyResponseDTO(policy.version(), policy.content(), policy.publishedAt(), policy.accepted(),
				policy.accepted() ? policy.acceptedAt() : null);
	}

}
