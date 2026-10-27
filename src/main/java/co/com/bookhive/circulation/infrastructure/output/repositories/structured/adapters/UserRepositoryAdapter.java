package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

	private static final String NAME = "name";
	private static final String EMAIL = "email";
	private static final String STATUS = "status";
	private static final char LIKE_ESCAPE = '\\';

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

		if (ObjectHelper.isNull(criteria)) {
			return List.of();
		}

		if (criteria.filtersByEmail()) {
			return repository.findAll(specificationOf(criteria))
					.stream().map(mapper::toEntity).toList();
		}

		if (criteria.filtersByText()) {
			return repository.findAll(notPurgedMatching(criteria.getText()),
					PageRequest.of(0, criteria.getLimit(), Sort.by(NAME, EMAIL)))
					.stream().map(mapper::toEntity).toList();
		}

		return List.of();
	}

	private static Specification<UserJpaEntity> specificationOf(final UserCriteria criteria) {
		return (root, query, builder) -> builder.equal(
				builder.lower(root.<String>get(EMAIL)), criteria.getEmail());
	}

	private static Specification<UserJpaEntity> notPurgedMatching(final String text) {
		final String pattern = "%" + escapeLike(text) + "%";
		return (root, query, builder) -> builder.and(
				builder.notEqual(root.get(STATUS), UserJpaEntity.STATUS_PURGED),
				builder.or(
						builder.like(builder.function("translate", String.class, builder.lower(root.<String>get(NAME)),
								builder.literal("áéíóúüñ"), builder.literal("aeiouun")), pattern, LIKE_ESCAPE),
						builder.like(builder.lower(root.<String>get(EMAIL)), pattern, LIKE_ESCAPE)));
	}

	static String escapeLike(final String text) {
		return text
				.replace(String.valueOf(LIKE_ESCAPE), String.valueOf(LIKE_ESCAPE) + LIKE_ESCAPE)
				.replace("%", LIKE_ESCAPE + "%")
				.replace("_", LIKE_ESCAPE + "_");
	}

}
