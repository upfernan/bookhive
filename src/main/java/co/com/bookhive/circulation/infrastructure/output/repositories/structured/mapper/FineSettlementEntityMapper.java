package co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineSettlementJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineSettlementEntity;

@Mapper(
		componentModel = "spring",
		implementationPackage = "<PACKAGE_NAME>.impl",
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface FineSettlementEntityMapper extends EntityMapper<FineSettlementEntity, FineSettlementJpaEntity> {

	@Override
	FineSettlementEntity toEntity(FineSettlementJpaEntity jpaEntity);

	@Override
	@Mapping(target = "settledAt", ignore = true)
	FineSettlementJpaEntity toJpaEntity(FineSettlementEntity entity);

}
