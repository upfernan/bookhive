package co.com.bookhive.access.services.output.ports.repositories;

import co.com.bookhive.access.services.output.ports.repositories.criteria.PermissionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.PermissionEntity;
import co.com.bookhive.shared.services.output.ports.repositories.FindByCriteriaRepository;

public interface PermissionRepository extends FindByCriteriaRepository<PermissionEntity, PermissionCriteria> {

}
