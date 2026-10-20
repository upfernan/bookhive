package co.com.bookhive.gateway.services.input.adapters.mapper;

import co.com.bookhive.gateway.domain.attempt.AttemptRequest;
import co.com.bookhive.gateway.domain.attempt.GuardVerdict;
import co.com.bookhive.gateway.services.input.dto.AttemptDTO;
import co.com.bookhive.gateway.services.input.dto.VerdictDTO;

public final class AttemptMapper {

	private AttemptMapper() {
		super();
	}

	public static AttemptRequest toDomain(final AttemptDTO attempt) {

		if (attempt == null) {
			return new AttemptRequest(null, null, null, false);
		}

		return new AttemptRequest(attempt.action(), attempt.account(), attempt.origin(), attempt.administrative());
	}

	public static VerdictDTO toDto(final GuardVerdict verdict) {

		final GuardVerdict safeVerdict = verdict == null ? GuardVerdict.admit() : verdict;

		return new VerdictDTO(
				safeVerdict.decision() == GuardVerdict.Decision.ADMIT,
				safeVerdict.decision() == GuardVerdict.Decision.UNAVAILABLE,
				safeVerdict.retryAfterSeconds());
	}

}
