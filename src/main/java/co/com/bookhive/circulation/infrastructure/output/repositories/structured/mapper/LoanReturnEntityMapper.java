package co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanReturnJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanReturnEntity;

@Mapper(
		componentModel = "spring",
		implementationPackage = "<PACKAGE_NAME>.impl",
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface LoanReturnEntityMapper extends EntityMapper<LoanReturnEntity, LoanReturnJpaEntity> {

	@Override
	LoanReturnEntity toEntity(LoanReturnJpaEntity jpaEntity);

	@Override
	@Mapping(target = "createdAt", ignore = true)
	LoanReturnJpaEntity toJpaEntity(LoanReturnEntity entity);

}
