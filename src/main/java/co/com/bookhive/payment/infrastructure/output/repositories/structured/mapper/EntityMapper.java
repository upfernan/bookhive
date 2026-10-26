package co.com.bookhive.payment.infrastructure.output.repositories.structured.mapper;

public interface EntityMapper<E, J> {

	E toEntity(J jpaEntity);

	J toJpaEntity(E entity);

}
