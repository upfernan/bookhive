package co.com.bookhive.circulation.services.usecase.user.findusercontact.impl;

import co.com.bookhive.circulation.domain.user.findusercontact.FindUserContactDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.circulation.services.usecase.user.findusercontact.FindUserContactUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class FindUserContactUseCaseImpl implements FindUserContactUseCase {

	private final UserRepository userRepository;

	public FindUserContactUseCaseImpl(final UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	public FindUserContactDomain execute(final FindUserContactDomain domain) {
		final FindUserContactDomain request =
				ObjectHelper.getDefaultValue(domain, new FindUserContactDomain(null, "", ""));
		return userRepository.findById(request.user())
				.filter(user -> UserEntity.STATUS_ACTIVE.equals(user.getStatus()))
				.map(user -> new FindUserContactDomain(user.getId(), user.getName(), user.getEmail()))
				.orElseGet(() -> new FindUserContactDomain(request.user(), "", ""));
	}

}
