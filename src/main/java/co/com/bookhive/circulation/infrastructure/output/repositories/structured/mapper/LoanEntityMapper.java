package co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.LoanJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.LoanEntity;

@Mapper(
		componentModel = "spring",
		implementationPackage = "<PACKAGE_NAME>.impl",
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
		uses = { BookCopyEntityMapper.class, UserEntityMapper.class })
public interface LoanEntityMapper extends EntityMapper<LoanEntity, LoanJpaEntity> {

	@Override
	LoanEntity toEntity(LoanJpaEntity jpaEntity);

	@Override
	@Mapping(target = "createdAt", ignore = true)
	LoanJpaEntity toJpaEntity(LoanEntity entity);

}
