package co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.BookCopyJpaEntity;

@Repository
public interface BookCopyJpaRepository
		extends JpaRepository<BookCopyJpaEntity, UUID>,
		JpaSpecificationExecutor<BookCopyJpaEntity> {

}
