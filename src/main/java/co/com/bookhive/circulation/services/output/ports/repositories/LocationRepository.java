package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.List;
import java.util.UUID;

import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities.BranchEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities.LevelEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities.ValueEntity;

public interface LocationRepository {

	List<BranchEntity> findBranches();

	void createBranch(BranchEntity branch);

	List<LevelEntity> findLevels();

	void replaceLevels(List<LevelEntity> levels);

	boolean hasValuesDeeperThan(int depth);

	List<ValueEntity> findValues(UUID bookCopy);

	void relocate(UUID bookCopy, UUID branch, List<ValueEntity> values);

	void clearValues(UUID bookCopy);

}
