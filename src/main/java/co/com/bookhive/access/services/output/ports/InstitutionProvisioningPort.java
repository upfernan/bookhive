package co.com.bookhive.access.services.output.ports;

import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities;

public interface InstitutionProvisioningPort {

	RegistrationEntities.DomainInstitution provision(String domain);

}
