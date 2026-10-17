package co.com.bookhive.circulation.testdata;

import java.util.function.Function;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;

public final class FakeEntityMapper<E, J> implements EntityMapper<E, J> {

	private final Function<J, E> toEntity;
	private final Function<E, J> toJpaEntity;

	public FakeEntityMapper(final Function<J, E> toEntity, final Function<E, J> toJpaEntity) {
		this.toEntity = toEntity;
		this.toJpaEntity = toJpaEntity;
	}

	@Override
	public E toEntity(final J jpaEntity) {
		return toEntity.apply(jpaEntity);
	}

	@Override
	public J toJpaEntity(final E entity) {
		return toJpaEntity.apply(entity);
	}

}
