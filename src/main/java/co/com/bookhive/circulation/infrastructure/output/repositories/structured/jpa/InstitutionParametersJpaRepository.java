package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.InstitutionParametersJpaEntity;

@Repository
public interface InstitutionParametersJpaRepository
		extends JpaRepository<InstitutionParametersJpaEntity, UUID> {

}
