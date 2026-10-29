package co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.UserJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.UserEntity;

@Mapper(
		componentModel = "spring",
		implementationPackage = "<PACKAGE_NAME>.impl",
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface UserEntityMapper extends EntityMapper<UserEntity, UserJpaEntity> {

	@Override
	UserEntity toEntity(UserJpaEntity jpaEntity);

	@Override
	@Mapping(target = "createdAt", ignore = true)
	UserJpaEntity toJpaEntity(UserEntity entity);

}
