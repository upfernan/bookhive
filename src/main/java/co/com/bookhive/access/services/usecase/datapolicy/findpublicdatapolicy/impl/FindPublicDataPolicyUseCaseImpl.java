package co.com.bookhive.access.services.usecase.datapolicy.findpublicdatapolicy.impl;

import java.util.Optional;
import co.com.bookhive.access.domain.datapolicy.findpublicdatapolicy.PublicDataPolicyDomain;
import co.com.bookhive.access.domain.session.registerperson.RegistrationDomain;
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
		final Optional<RegistrationEntities.DomainInstitution> institution = registrations.institutionOfDomain(normalized);
		final Optional<RegistrationEntities.PublicPolicy> found = institution.isPresent()
				? registrations.currentPolicyOf(institution.get())
				: defaultPolicyFor(normalized);
		final RegistrationEntities.PublicPolicy policy = found
				.orElseThrow(() -> PublicDataPolicyDomain.notFound("No hay aviso de datos para " + normalized + "."));
		return new PublicDataPolicyDomain(policy.version(), policy.content(), policy.publishedAt());
	}

	private Optional<RegistrationEntities.PublicPolicy> defaultPolicyFor(final String domain) {
		return RegistrationDomain.isEducational(domain) ? registrations.defaultPolicy() : Optional.empty();
	}

}
