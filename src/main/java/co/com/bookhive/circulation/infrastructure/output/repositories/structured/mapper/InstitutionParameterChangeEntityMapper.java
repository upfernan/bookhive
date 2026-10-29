package co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.InstitutionParameterChangeJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.InstitutionParameterChangeEntity;

@Mapper(
		componentModel = "spring",
		implementationPackage = "<PACKAGE_NAME>.impl",
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface InstitutionParameterChangeEntityMapper
		extends EntityMapper<InstitutionParameterChangeEntity, InstitutionParameterChangeJpaEntity> {

	@Override
	InstitutionParameterChangeEntity toEntity(InstitutionParameterChangeJpaEntity jpaEntity);

	@Override
	@Mapping(target = "changedAt", ignore = true)
	InstitutionParameterChangeJpaEntity toJpaEntity(InstitutionParameterChangeEntity entity);

}
