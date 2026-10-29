package co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.ReservationJpaEntity;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.ReservationEntity;

@Mapper(
		componentModel = "spring",
		implementationPackage = "<PACKAGE_NAME>.impl",
		nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface ReservationEntityMapper extends EntityMapper<ReservationEntity, ReservationJpaEntity> {

	@Override
	ReservationEntity toEntity(ReservationJpaEntity jpaEntity);

	@Override
	ReservationJpaEntity toJpaEntity(ReservationEntity entity);

}
