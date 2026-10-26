package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.LocationJpaRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.LocationRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities.BranchEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities.LevelEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LocationEntities.ValueEntity;

@Component
public class LocationRepositoryAdapter implements LocationRepository {

	private final LocationJpaRepository repository;

	public LocationRepositoryAdapter(final LocationJpaRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public List<BranchEntity> findBranches() {
		return repository.findBranches().stream().map(row -> new BranchEntity(row.getId(), row.getName())).toList();
	}

	@Override
	public void createBranch(final BranchEntity branch) {
		repository.createBranch(branch.id(), branch.name());
	}

	@Override
	public List<LevelEntity> findLevels() {
		return repository.findLevels().stream().map(row -> new LevelEntity(row.getDepth(), row.getText())).toList();
	}

	@Override
	public void replaceLevels(final List<LevelEntity> levels) {
		repository.deleteLevelsDeeperThan(levels.size());
		levels.forEach(level -> repository.saveLevel(level.depth(), level.name()));
	}

	@Override
	public boolean hasValuesDeeperThan(final int depth) {
		return repository.countValuesDeeperThan(depth) > 0;
	}

	@Override
	public List<ValueEntity> findValues(final UUID bookCopy) {
		return repository.findValues(bookCopy).stream().map(row -> new ValueEntity(row.getDepth(), row.getText())).toList();
	}

	@Override
	public void relocate(final UUID bookCopy, final UUID branch, final List<ValueEntity> values) {
		repository.changeBranch(bookCopy, branch);
		repository.deleteValues(bookCopy);
		values.forEach(value -> repository.saveValue(bookCopy, value.depth(), value.value()));
	}

	@Override
	public void clearValues(final UUID bookCopy) {
		repository.deleteValues(bookCopy);
	}

}
