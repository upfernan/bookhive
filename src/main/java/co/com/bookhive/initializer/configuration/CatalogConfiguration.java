package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.catalog.services.input.adapters.SearchBooksInputAdapter;
import co.com.bookhive.catalog.services.input.adapters.SyncAllCatalogsInputAdapter;
import co.com.bookhive.catalog.services.input.adapters.SyncCatalogInputAdapter;
import co.com.bookhive.catalog.services.input.adapters.mapper.SearchBooksMapperImpl;
import co.com.bookhive.catalog.services.input.ports.SearchBooksInputPort;
import co.com.bookhive.catalog.services.input.ports.SyncAllCatalogsInputPort;
import co.com.bookhive.catalog.services.input.ports.SyncCatalogInputPort;
import co.com.bookhive.catalog.services.output.ports.BookCopiesPort;
import co.com.bookhive.catalog.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.catalog.services.output.ports.TenantScopePort;
import co.com.bookhive.catalog.services.output.ports.repositories.BookIndexRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.BookRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.CatalogChangeRepository;
import co.com.bookhive.catalog.services.usecase.book.searchbooks.SearchBooksUseCase;
import co.com.bookhive.catalog.services.usecase.book.searchbooks.impl.SearchBooksUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalog.syncallcatalogs.SyncAllCatalogsUseCase;
import co.com.bookhive.catalog.services.usecase.catalog.syncallcatalogs.impl.SyncAllCatalogsUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalog.synccatalog.SyncCatalogUseCase;
import co.com.bookhive.catalog.services.usecase.catalog.synccatalog.impl.SyncCatalogUseCaseImpl;

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

}
