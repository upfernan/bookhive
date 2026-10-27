package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.InstitutionParameterChangeJpaEntity;


public interface InstitutionParameterChangeJpaRepository extends JpaRepository<InstitutionParameterChangeJpaEntity, UUID> {
}
