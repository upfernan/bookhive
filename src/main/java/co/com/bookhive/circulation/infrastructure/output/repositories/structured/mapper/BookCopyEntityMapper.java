package co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.BookCopyJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;

@Mapper(
		componentModel = "spring",
		implementationPackage = "<PACKAGE_NAME>.impl",
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface BookCopyEntityMapper extends EntityMapper<BookCopyEntity, BookCopyJpaEntity> {

	@Override
	@Mapping(target = "createdAt", ignore = true)
	BookCopyJpaEntity toJpaEntity(BookCopyEntity entity);

}
