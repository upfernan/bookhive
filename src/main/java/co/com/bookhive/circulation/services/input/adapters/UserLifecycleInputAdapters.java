package co.com.bookhive.circulation.services.input.adapters;

import java.util.Arrays;

import co.com.bookhive.circulation.domain.account.manageuser.UserLifecycleDomains;
import co.com.bookhive.circulation.services.input.dto.request.UserLifecycleRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.UserLifecycleResponseDTOs;
import co.com.bookhive.circulation.services.input.ports.ChangeUserStatusInputPort;
import co.com.bookhive.circulation.services.input.ports.ConfigureRetentionInputPort;
import co.com.bookhive.circulation.services.input.ports.PurgeExpiredUsersInputPort;
import co.com.bookhive.circulation.services.usecase.account.changeuserstatus.ChangeUserStatusUseCase;
import co.com.bookhive.circulation.services.usecase.account.configureretention.ConfigureRetentionUseCase;
import co.com.bookhive.circulation.services.usecase.account.purgeexpiredusers.PurgeExpiredUsersUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class UserLifecycleInputAdapters {

	private UserLifecycleInputAdapters() {
		super();
	}

	public static ChangeUserStatusInputPort changeStatus(final ChangeUserStatusUseCase useCase) {
		return request -> {
			final UserLifecycleRequestDTO safe = ObjectHelper.getDefaultValue(request, new UserLifecycleRequestDTO(null, null));
			final UserLifecycleDomains.Change change = Arrays.stream(UserLifecycleDomains.Change.values())
					.filter(value -> value.name().equals(safe.change()))
					.findFirst()
					.orElse(UserLifecycleDomains.Change.DEACTIVATE);
			final UserLifecycleDomains.Account account = useCase.execute(new UserLifecycleDomains.Request(safe.user(), change));
			return new UserLifecycleResponseDTOs.Account(account.user(), account.status(), account.deactivatedAt());
		};
	}

	public static ConfigureRetentionInputPort configureRetention(final ConfigureRetentionUseCase useCase) {
		return request -> new UserLifecycleResponseDTOs.Retention(useCase.execute(
				ObjectHelper.isNull(request) ? null : new UserLifecycleDomains.Retention(request.months())).months());
	}

	public static PurgeExpiredUsersInputPort purgeExpired(final PurgeExpiredUsersUseCase useCase) {
		return institution -> {
			final UserLifecycleDomains.PurgeRun run = useCase.execute(institution);
			return new UserLifecycleResponseDTOs.PurgeRun(run.institution(), run.purged(), run.skipped());
		};
	}

}
