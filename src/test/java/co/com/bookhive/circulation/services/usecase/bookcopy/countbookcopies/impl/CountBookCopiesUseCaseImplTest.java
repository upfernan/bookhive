package co.com.bookhive.circulation.services.usecase.bookcopy.countbookcopies.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.circulation.domain.bookcopy.countbookcopies.BookCopiesCountDomain;
import co.com.bookhive.circulation.domain.bookcopy.countbookcopies.CountBookCopiesDomain;
import co.com.bookhive.circulation.services.output.ports.repositories.entities.BookCopyEntity;
import co.com.bookhive.circulation.testdata.FakeRepositories;

class CountBookCopiesUseCaseImplTest {

	private final UUID cleanCode = UUID.randomUUID();
	private final UUID refactoring = UUID.randomUUID();
	private final UUID withoutCopies = UUID.randomUUID();

	@Test
	@DisplayName("cuenta por libro los ejemplares disponibles y los que están en servicio, sin contar los dados de baja")
	void countsCopiesPerBook() {
		final FakeRepositories.BookCopies bookCopies = new FakeRepositories.BookCopies()
				.with(FakeRepositories.bookCopyOf(cleanCode, BookCopyEntity.STATUS_AVAILABLE))
				.with(FakeRepositories.bookCopyOf(cleanCode, BookCopyEntity.STATUS_AVAILABLE))
				.with(FakeRepositories.bookCopyOf(cleanCode, BookCopyEntity.STATUS_LENT))
				.with(FakeRepositories.bookCopyOf(cleanCode, BookCopyEntity.STATUS_WITHDRAWN))
				.with(FakeRepositories.bookCopyOf(refactoring, BookCopyEntity.STATUS_RESERVED));

		final List<BookCopiesCountDomain> counts = new CountBookCopiesUseCaseImpl(bookCopies)
				.execute(new CountBookCopiesDomain(List.of(cleanCode, refactoring, withoutCopies)));

		assertEquals(List.of(
				new BookCopiesCountDomain(cleanCode, 2, 3),
				new BookCopiesCountDomain(refactoring, 0, 1),
				new BookCopiesCountDomain(withoutCopies, 0, 0)), counts);
	}

	@Test
	@DisplayName("sin libros no consulta ejemplares")
	void returnsNothingWithoutBooks() {
		final CountBookCopiesUseCaseImpl useCase = new CountBookCopiesUseCaseImpl(new FakeRepositories.BookCopies());

		assertEquals(List.of(), useCase.execute(null));
		assertEquals(List.of(), useCase.execute(new CountBookCopiesDomain(List.of())));
	}

}
