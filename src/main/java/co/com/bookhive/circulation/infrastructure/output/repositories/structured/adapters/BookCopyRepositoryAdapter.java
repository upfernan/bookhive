package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.BookCopyJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.BookCopyJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;

@Component
public class BookCopyRepositoryAdapter implements BookCopyRepository {

	private final BookCopyJpaRepository repository;
	private final EntityMapper<BookCopyEntity, BookCopyJpaEntity> mapper;

	public BookCopyRepositoryAdapter(
			final BookCopyJpaRepository repository,
			final EntityMapper<BookCopyEntity, BookCopyJpaEntity> mapper) {

		super();
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public Optional<BookCopyEntity> findById(final UUID id) {
		return repository.findById(id).map(mapper::toEntity);
	}

	@Override
	public List<BookCopyEntity> findAll() {
		return repository.findAll().stream().map(mapper::toEntity).toList();
	}

	@Override
	public void update(final BookCopyEntity entity) {
		repository.save(mapper.toJpaEntity(entity));
	}

}
