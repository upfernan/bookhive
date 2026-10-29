package co.com.bookhive.catalog.services.usecase.catalog.syncallcatalogs.impl;

import java.util.ArrayList;
import java.util.List;
import co.com.bookhive.catalog.domain.catalog.syncallcatalogs.SyncAllCatalogsDomain;
import co.com.bookhive.catalog.services.output.ports.InstitutionDirectoryPort;
import co.com.bookhive.catalog.services.output.ports.TenantScopePort;
import co.com.bookhive.catalog.services.usecase.catalog.syncallcatalogs.SyncAllCatalogsUseCase;
import co.com.bookhive.catalog.services.usecase.catalog.synccatalog.SyncCatalogUseCase;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.ObjectHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;

public final class SyncAllCatalogsUseCaseImpl implements SyncAllCatalogsUseCase {

	private final InstitutionDirectoryPort institutionDirectory;
	private final TenantScopePort tenantScope;
	private final SyncCatalogUseCase syncCatalog;

	public SyncAllCatalogsUseCaseImpl(
			final InstitutionDirectoryPort institutionDirectory,
			final TenantScopePort tenantScope,
			final SyncCatalogUseCase syncCatalog) {

		super();
		this.institutionDirectory = requireDependency(institutionDirectory, "el directorio de instituciones");
		this.tenantScope = requireDependency(tenantScope, "el cambio de institución");
		this.syncCatalog = requireDependency(syncCatalog, "la sincronización de un catálogo");
	}

	@Override
	public SyncAllCatalogsDomain execute() {
		final List<String> synced = new ArrayList<>();
		final List<String> failed = new ArrayList<>();

		for (final String schema : institutionDirectory.activeSchemas()) {
			try {
				tenantScope.within(schema, syncCatalog::execute);
				synced.add(schema);
			} catch (final RuntimeException exception) {
				failed.add(schema + ": " + reasonOf(exception));
			}
		}

		return new SyncAllCatalogsDomain(synced, failed);
	}

	private static String reasonOf(final RuntimeException exception) {
		return exception instanceof BookHiveException bookHive
				? bookHive.getTechnicalMessage()
				: exception.getClass().getSimpleName() + " " + exception.getMessage();
	}

	private static <T> T requireDependency(final T dependency, final String name) {
		if (ObjectHelper.isNull(dependency)) {
			throw BookHiveException.createUnexpected(
					MessageKey.REQUEST_UNEXPECTED,
					"El caso de uso SyncAllCatalogs se creó sin " + name + ".");
		}
		return dependency;
	}

}
