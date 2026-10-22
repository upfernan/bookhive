package co.com.bookhive.catalog.infrastructure.output.repositories.structured.adapters;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.BookDetailsRow;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.BookJpaRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.BookRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.BookCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.BookEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;

@Component
public class BookRepositoryAdapter implements BookRepository {

	private final BookJpaRepository repository;

	public BookRepositoryAdapter(final BookJpaRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public List<BookEntity> findByCriteria(final BookCriteria criteria) {
		if (ObjectHelper.isNull(criteria)) {
			return List.of();
		}
		if (criteria.filtersByIds()) {
			return repository.findDetailsByIds(criteria.getIds()).stream().map(BookRepositoryAdapter::toEntity).toList();
		}
		if (TextHelper.isDefaultValue(criteria.getStatus())) {
			return List.of();
		}
		final PageCriteria page = criteria.getPage();
		return repository.findDetailsByStatus(criteria.getStatus(), page.getSize(), (long) page.getPage() * page.getSize())
				.stream().map(BookRepositoryAdapter::toEntity).toList();
	}

	@Override
	public long countActive() {
		return repository.countByStatus(BookEntity.STATUS_ACTIVE);
	}

	private static BookEntity toEntity(final BookDetailsRow row) {
		return new BookEntity(
				row.getId(),
				row.getTitle(),
				row.getIsbn(),
				ObjectHelper.getDefaultValue(row.getYear(), 0),
				row.getFormat(),
				row.getStatus(),
				row.getCategory(),
				row.getPublisher(),
				authorsOf(row.getAuthors()));
	}

	private static List<String> authorsOf(final String authors) {
		if (TextHelper.isDefaultValue(authors)) {
			return List.of();
		}
		return Arrays.stream(authors.split(BookJpaRepository.AUTHOR_SEPARATOR))
				.map(String::trim)
				.filter(author -> !author.isEmpty())
				.toList();
	}

}
