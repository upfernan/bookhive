package co.com.bookhive.circulation.infrastructure.output.repositories.structured.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.BookCopyJpaRepository;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.jpa.entities.BookCopyJpaEntity;
import co.com.bookhive.circulation.infrastructure.output.repositories.structured.mapper.EntityMapper;
import co.com.bookhive.circulation.services.output.ports.repositories.BookCopyRepository;
import co.com.bookhive.circulation.services.output.ports.repositories.criteria.BookCopyCriteria;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.helper.TextHelper;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.shared.services.output.ports.repositories.criteria.PageCriteria;
import jakarta.persistence.criteria.Predicate;

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
	public List<BookCopyEntity> findAll(final PageCriteria page) {
		final PageCriteria safePage = ObjectHelper.getDefaultValue(page, PageCriteria.defaultPage());
		return repository.findAll(PageRequest.of(safePage.getPage(), safePage.getSize()))
				.stream().map(mapper::toEntity).toList();
	}

	@Override
	public List<BookCopyEntity> findByCriteria(final BookCopyCriteria criteria) {
		if (ObjectHelper.isNull(criteria) || (!criteria.filtersByBooks() && !criteria.filtersByBarcode())) {
			return List.of();
		}
		return repository.findAll(specificationOf(criteria))
				.stream().map(mapper::toEntity).toList();
	}

	@Override
	public void update(final BookCopyEntity entity) {
		repository.save(mapper.toJpaEntity(entity));
	}

	@Override
	public boolean changeStatus(final UUID bookCopy, final String expectedStatus, final String newStatus) {
		if (UUIDHelper.isDefaultValue(bookCopy)
				|| TextHelper.isDefaultValue(expectedStatus)
				|| TextHelper.isDefaultValue(newStatus)) {
			return false;
		}
		return repository.changeStatus(
				bookCopy, TextHelper.getDefaultValue(expectedStatus), TextHelper.getDefaultValue(newStatus)) == 1;
	}

	private static Specification<BookCopyJpaEntity> specificationOf(final BookCopyCriteria criteria) {
		return (root, query, builder) -> {
			final List<Predicate> predicates = new ArrayList<>();
			if (criteria.filtersByBooks()) {
				predicates.add(root.get("book").in(criteria.getBooks()));
			}
			if (criteria.filtersByBarcode()) {
				predicates.add(builder.equal(root.get("barcode"), criteria.getBarcode()));
			}
			if (criteria.excludesStatus()) {
				predicates.add(builder.notEqual(root.get("status"), criteria.getExcludedStatus()));
			}
			return builder.and(predicates.toArray(new Predicate[0]));
		};
	}

}
