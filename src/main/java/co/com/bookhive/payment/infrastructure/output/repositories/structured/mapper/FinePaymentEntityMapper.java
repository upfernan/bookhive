package co.com.bookhive.payment.infrastructure.output.repositories.structured.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import co.com.bookhive.payment.infrastructure.output.repositories.structured.jpa.entities.FinePaymentJpaEntity;
import co.com.bookhive.payment.services.output.ports.repositories.entities.FinePaymentEntity;

@Mapper(
		componentModel = "spring",
		implementationPackage = "<PACKAGE_NAME>.impl",
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface FinePaymentEntityMapper extends EntityMapper<FinePaymentEntity, FinePaymentJpaEntity> {

	@Override
	FinePaymentEntity toEntity(FinePaymentJpaEntity jpaEntity);

	@Override
	@Mapping(target = "createdAt", ignore = true)
	FinePaymentJpaEntity toJpaEntity(FinePaymentEntity entity);

}
