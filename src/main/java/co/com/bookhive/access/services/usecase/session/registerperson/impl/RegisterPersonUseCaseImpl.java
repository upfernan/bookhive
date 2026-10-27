package co.com.bookhive.access.services.usecase.session.registerperson.impl;

import java.util.Optional;

import co.com.bookhive.access.domain.session.registerperson.RegistrationDomain;
import co.com.bookhive.access.services.output.ports.IdentityProviderPort;
import co.com.bookhive.access.services.output.ports.InstitutionProvisioningPort;
import co.com.bookhive.access.services.output.ports.entities.VerifiedIdentityEntity;
import co.com.bookhive.access.services.output.ports.repositories.RegistrationRepository;
import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities;
import co.com.bookhive.access.services.usecase.session.registerperson.RegisterPersonUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

public final class RegisterPersonUseCaseImpl implements RegisterPersonUseCase {

	private final IdentityProviderPort identityProvider;
	private final RegistrationRepository registrations;
	private final InstitutionProvisioningPort provisioning;
	private final AuditPort audit;

	public RegisterPersonUseCaseImpl(
			final IdentityProviderPort identityProvider,
			final RegistrationRepository registrations,
			final InstitutionProvisioningPort provisioning,
			final AuditPort audit) {

		super();
		this.identityProvider = identityProvider;
		this.registrations = registrations;
		this.provisioning = provisioning;
		this.audit = audit;
	}

	@Override
	public RegistrationDomain.Registered execute(final RegistrationDomain input) {

		final RegistrationDomain registration = ObjectHelper.getDefaultValue(input, new RegistrationDomain(null, null, 0));
		registration.validate();

		final VerifiedIdentityEntity identity = identityProvider.verify(registration.idToken());
		if (!identity.emailVerified()) {
			throw BookHiveException.createNotAllowed(MessageKey.REGISTRATION_EMAIL_NOT_VERIFIED,
					"La identidad " + identity.identity() + " intentó registrarse sin confirmar su correo.");
		}
		if (registrations.identityRegistered(identity.identity())) {
			throw BookHiveException.createConflict(MessageKey.REGISTRATION_ALREADY_REGISTERED,
					"La identidad " + identity.identity() + " ya está registrada.");
		}

		final String domain = RegistrationDomain.domainOf(identity.email());
		final RegistrationEntities.DomainInstitution institution = registrations.institutionOfDomain(domain)
				.or(() -> provisionIfEducational(domain))
				.orElseThrow(() -> BookHiveException.createNotAllowed(MessageKey.REGISTRATION_DOMAIN_NOT_ALLOWED,
						"El dominio " + domain + " no es de una institución activa ni educativo."));

		final RegistrationEntities.NewPerson person = registrations.register(new RegistrationEntities.NewPerson(
				UUIDHelper.generateNew(), identity.identity(), registration.name(), identity.email(), registration.policyVersion()),
				institution);

		audit.record(AuditPort.PERSON_REGISTERED, institution.institution(), person.id(), domain);
		return new RegistrationDomain.Registered(person.id(), institution.schema(), identity.sessionToken());
	}

	private Optional<RegistrationEntities.DomainInstitution> provisionIfEducational(final String domain) {
		if (!RegistrationDomain.isEducational(domain)) {
			return Optional.empty();
		}
		final RegistrationEntities.DomainInstitution created = provisioning.provision(domain);
		audit.record(AuditPort.INSTITUTION_SELF_REGISTERED, created.institution(), null, domain + " -> " + created.schema());
		return Optional.of(created);
	}

}
