package co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.FineJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.FineEntity;

@Mapper(
		componentModel = "spring",
		implementationPackage = "<PACKAGE_NAME>.impl",
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface FineEntityMapper extends EntityMapper<FineEntity, FineJpaEntity> {

	@Override
	@Mapping(target = "createdAt", ignore = true)
	FineJpaEntity toJpaEntity(FineEntity entity);

}
