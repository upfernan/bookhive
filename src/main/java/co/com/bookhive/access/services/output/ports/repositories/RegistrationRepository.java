package co.com.bookhive.access.services.output.ports.repositories;

import java.util.Optional;

import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities.DomainInstitution;
import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities.NewPerson;
import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities.PublicPolicy;

public interface RegistrationRepository {

	Optional<DomainInstitution> institutionOfDomain(String domain);

	boolean identityRegistered(String identity);

	Optional<PublicPolicy> currentPolicyOf(DomainInstitution institution);

	NewPerson register(NewPerson person, DomainInstitution institution);

}
