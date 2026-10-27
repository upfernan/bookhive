package co.com.bookhive.access.services.usecase.session.describesession.impl;

import co.com.bookhive.access.domain.session.describesession.SessionProfileDomain;
import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.services.output.ports.repositories.PermissionRepository;
import co.com.bookhive.access.services.output.ports.repositories.SessionProfileRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.PermissionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.PermissionEntity;
import co.com.bookhive.access.services.output.ports.repositories.entities.SessionProfileEntity;
import co.com.bookhive.access.services.usecase.session.describesession.DescribeSessionUseCase;
import co.com.bookhive.access.services.usecase.session.resolvesession.ResolveSessionUseCase;

public final class DescribeSessionUseCaseImpl implements DescribeSessionUseCase {

	private final ResolveSessionUseCase resolveSession;
	private final SessionProfileRepository profiles;
	private final PermissionRepository permissions;

	public DescribeSessionUseCaseImpl(
			final ResolveSessionUseCase resolveSession,
			final SessionProfileRepository profiles,
			final PermissionRepository permissions) {

		super();
		this.resolveSession = resolveSession;
		this.profiles = profiles;
		this.permissions = permissions;
	}

	@Override
	public SessionProfileDomain execute() {
		final ResolveSessionDomain session = resolveSession.execute();
		final SessionProfileEntity profile = profiles.findProfile(session.person(), session.institution());
		return new SessionProfileDomain(profile.email(), profile.institution(), profile.roles(),
				permissions.findByCriteria(PermissionCriteria.grantedTo(session.person(), "")).stream()
						.map(PermissionEntity::getCode).distinct().toList());
	}

}
