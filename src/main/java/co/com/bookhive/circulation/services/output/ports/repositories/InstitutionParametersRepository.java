package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;

public interface InstitutionParametersRepository
		extends FindRepository<UUID, InstitutionParametersEntity> {

}
