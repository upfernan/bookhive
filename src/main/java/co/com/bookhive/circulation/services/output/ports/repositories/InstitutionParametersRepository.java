package co.com.bookhive.circulation.services.output.ports.repositories;

import co.com.bookhive.shared.services.output.ports.repositories.FindRepository;
import co.com.bookhive.shared.services.output.ports.repositories.UpdateRepository;

import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParametersEntity;

public interface InstitutionParametersRepository
		extends FindRepository<UUID, InstitutionParametersEntity>,
		UpdateRepository<InstitutionParametersEntity> {

}
