package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.BookCopyJpaEntity;

public interface BookCopyJpaRepository extends JpaRepository<BookCopyJpaEntity, UUID> {

}
