package co.com.bookhive.access.services.usecase.datapolicy.findpublicdatapolicy.impl;

import co.com.bookhive.access.domain.datapolicy.findpublicdatapolicy.PublicDataPolicyDomain;
import co.com.bookhive.access.services.output.ports.repositories.RegistrationRepository;
import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities;
import co.com.bookhive.access.services.usecase.datapolicy.findpublicdatapolicy.FindPublicDataPolicyUseCase;

public final class FindPublicDataPolicyUseCaseImpl implements FindPublicDataPolicyUseCase {

	private final RegistrationRepository registrations;

	public FindPublicDataPolicyUseCaseImpl(final RegistrationRepository registrations) {
		super();
		this.registrations = registrations;
	}

	@Override
	public PublicDataPolicyDomain execute(final String domain) {
		final String normalized = PublicDataPolicyDomain.normalizedDomain(domain);
		final RegistrationEntities.DomainInstitution institution = registrations.institutionOfDomain(normalized)
				.orElseThrow(() -> PublicDataPolicyDomain.notFound("El dominio " + normalized + " no está registrado."));
		final RegistrationEntities.PublicPolicy policy = registrations.currentPolicyOf(institution)
				.orElseThrow(() -> PublicDataPolicyDomain.notFound("La institución de " + normalized + " no tiene aviso publicado."));
		return new PublicDataPolicyDomain(policy.version(), policy.content(), policy.publishedAt());
	}

}
