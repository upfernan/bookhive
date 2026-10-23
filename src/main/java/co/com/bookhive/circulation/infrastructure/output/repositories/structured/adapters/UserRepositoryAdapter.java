package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.UserJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.UserJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.UserRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.UserCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

@Component
public class UserRepositoryAdapter implements UserRepository {

	private final UserJpaRepository repository;
	private final EntityMapper<UserEntity, UserJpaEntity> mapper;

	public UserRepositoryAdapter(
			final UserJpaRepository repository,
			final EntityMapper<UserEntity, UserJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public Optional<UserEntity> findById(final UUID id) {

		if (UUIDHelper.isDefaultValue(id)) {
			return Optional.empty();
		}

		return repository.findById(id).map(mapper::toEntity);
	}

	@Override
	public List<UserEntity> findAll(final PageCriteria page) {

		final PageCriteria safePage = ObjectHelper.getDefaultValue(page, PageCriteria.defaultPage());

		return repository.findAll(PageRequest.of(safePage.getPage(), safePage.getSize()))
				.stream().map(mapper::toEntity).toList();
	}

	@Override
	public List<UserEntity> findByCriteria(final UserCriteria criteria) {

		if (ObjectHelper.isNull(criteria) || !criteria.filtersByEmail()) {
			return List.of();
		}

		return repository.findAll(specificationOf(criteria))
				.stream().map(mapper::toEntity).toList();
	}

	private static Specification<UserJpaEntity> specificationOf(final UserCriteria criteria) {
		return (root, query, builder) -> builder.equal(
				builder.lower(root.<String>get("email")), criteria.getEmail());
	}

}
