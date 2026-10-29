package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import co.com.bookhive.catalog.services.input.adapters.FindBookCopyDetailsInputAdapter;
import co.com.bookhive.catalog.services.input.adapters.FindBookTitleInputAdapter;
import co.com.bookhive.catalog.services.input.adapters.ListMyReservationsInputAdapter;
import co.com.bookhive.catalog.services.input.adapters.ViewMyAccountInputAdapter;
import co.com.bookhive.catalog.services.input.ports.ViewMyAccountInputPort;
import co.com.bookhive.catalog.services.output.ports.BranchDirectoryPort;
import co.com.bookhive.catalog.services.output.ports.UserAccountPort;
import co.com.bookhive.catalog.services.usecase.account.viewmyaccount.impl.ViewMyAccountUseCaseImpl;
import co.com.bookhive.catalog.services.input.adapters.SearchBooksInputAdapter;
import co.com.bookhive.catalog.services.input.adapters.SyncAllCatalogsInputAdapter;
import co.com.bookhive.catalog.services.input.adapters.SyncCatalogInputAdapter;
import co.com.bookhive.catalog.services.input.adapters.mapper.FindBookCopyDetailsMapperImpl;
import co.com.bookhive.catalog.services.input.adapters.mapper.SearchBooksMapperImpl;
import co.com.bookhive.catalog.services.input.ports.FindBookCopyDetailsInputPort;
import co.com.bookhive.catalog.services.input.ports.FindBookTitleInputPort;
import co.com.bookhive.catalog.services.input.ports.ListMyReservationsInputPort;
import co.com.bookhive.catalog.services.input.ports.SearchBooksInputPort;
import co.com.bookhive.catalog.services.input.ports.SyncAllCatalogsInputPort;
import co.com.bookhive.catalog.services.input.ports.SyncCatalogInputPort;
import co.com.bookhive.catalog.services.output.ports.BookCopiesPort;
import co.com.bookhive.catalog.services.output.ports.BookCopyStatusPort;
import co.com.bookhive.catalog.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.catalog.services.output.ports.TenantScopePort;
import co.com.bookhive.catalog.services.output.ports.UserReservationsPort;
import co.com.bookhive.catalog.services.output.ports.repositories.BookIndexRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.BookRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.CatalogChangeRepository;
import co.com.bookhive.catalog.services.usecase.book.findbooktitle.FindBookTitleUseCase;
import co.com.bookhive.catalog.services.usecase.book.findbooktitle.impl.FindBookTitleUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.book.searchbooks.SearchBooksUseCase;
import co.com.bookhive.catalog.services.usecase.bookcopy.findbookcopydetails.FindBookCopyDetailsUseCase;
import co.com.bookhive.catalog.services.usecase.bookcopy.findbookcopydetails.impl.FindBookCopyDetailsUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.book.searchbooks.impl.SearchBooksUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalog.syncallcatalogs.SyncAllCatalogsUseCase;
import co.com.bookhive.catalog.services.usecase.catalog.syncallcatalogs.impl.SyncAllCatalogsUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalog.synccatalog.SyncCatalogUseCase;
import co.com.bookhive.catalog.services.usecase.catalog.synccatalog.impl.SyncCatalogUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.reservation.listmyreservations.ListMyReservationsUseCase;
import co.com.bookhive.catalog.services.usecase.reservation.listmyreservations.impl.ListMyReservationsUseCaseImpl;

@Configuration
public class CatalogConfiguration {

	@Bean
	SearchBooksUseCase searchBooksUseCase(final BookIndexRepository bookIndex, final BookCopiesPort bookCopies) {
		return new SearchBooksUseCaseImpl(bookIndex, bookCopies);
	}

	@Bean
	SearchBooksInputPort searchBooksInputPort(final SearchBooksUseCase searchBooksUseCase) {
		return new SearchBooksInputAdapter(searchBooksUseCase, new SearchBooksMapperImpl());
	}

	@Bean
	FindBookCopyDetailsUseCase findBookCopyDetailsUseCase(
			final BookCopyStatusPort bookCopyStatus,
			final BookRepository bookRepository,
			final BranchDirectoryPort branchDirectory) {
		return new FindBookCopyDetailsUseCaseImpl(bookCopyStatus, bookRepository, branchDirectory);
	}

	@Bean
	FindBookCopyDetailsInputPort findBookCopyDetailsInputPort(
			final FindBookCopyDetailsUseCase findBookCopyDetailsUseCase) {
		return new FindBookCopyDetailsInputAdapter(findBookCopyDetailsUseCase, new FindBookCopyDetailsMapperImpl());
	}

	@Bean
	SyncCatalogUseCase syncCatalogUseCase(
			final BookRepository bookRepository,
			final CatalogChangeRepository catalogChangeRepository,
			final BookIndexRepository bookIndex) {

		return new SyncCatalogUseCaseImpl(bookRepository, catalogChangeRepository, bookIndex);
	}

	@Bean
	SyncCatalogInputPort syncCatalogInputPort(final SyncCatalogUseCase syncCatalogUseCase) {
		return new SyncCatalogInputAdapter(syncCatalogUseCase);
	}

	@Bean
	SyncAllCatalogsUseCase syncAllCatalogsUseCase(
			final InstitutionDirectoryPort institutionDirectory,
			final TenantScopePort tenantScope,
			final SyncCatalogUseCase syncCatalogUseCase) {

		return new SyncAllCatalogsUseCaseImpl(institutionDirectory, tenantScope, syncCatalogUseCase);
	}

	@Bean
	SyncAllCatalogsInputPort syncAllCatalogsInputPort(final SyncAllCatalogsUseCase syncAllCatalogsUseCase) {
		return new SyncAllCatalogsInputAdapter(syncAllCatalogsUseCase);
	}

	@Bean
	FindBookTitleUseCase findBookTitleUseCase(final BookRepository bookRepository) {
		return new FindBookTitleUseCaseImpl(bookRepository);
	}

	@Bean
	FindBookTitleInputPort findBookTitleInputPort(final FindBookTitleUseCase findBookTitleUseCase) {
		return new FindBookTitleInputAdapter(findBookTitleUseCase);
	}
	@Bean
	ListMyReservationsUseCase listMyReservationsUseCase(
			final UserReservationsPort userReservations,
			final BookRepository bookRepository) {

		return new ListMyReservationsUseCaseImpl(userReservations, bookRepository);
	}

	@Bean
	ListMyReservationsInputPort listMyReservationsInputPort(
			final ListMyReservationsUseCase listMyReservationsUseCase) {

		return new ListMyReservationsInputAdapter(listMyReservationsUseCase);
	}

	@Bean
	ViewMyAccountInputPort viewMyAccountInputPort(
			final UserAccountPort userAccount,
			final ListMyReservationsUseCase listMyReservationsUseCase,
			final BookRepository bookRepository) {

		return new ViewMyAccountInputAdapter(
				new ViewMyAccountUseCaseImpl(userAccount, listMyReservationsUseCase, bookRepository));
	}
}
