package co.com.bookhive.access.services.output.ports.repositories;

import java.util.Optional;

import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyEntity;

public interface DataPolicyRepository {

	Optional<DataPolicyEntity> findCurrent();

}
