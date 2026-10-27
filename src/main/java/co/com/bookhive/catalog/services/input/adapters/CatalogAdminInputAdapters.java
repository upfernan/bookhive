package co.com.bookhive.catalog.services.input.adapters;

import co.com.bookhive.catalog.domain.catalogadmin.CatalogAdminDomains;
import co.com.bookhive.catalog.services.input.dto.request.CategoryRequestDTO;
import co.com.bookhive.catalog.services.input.dto.request.CategoryStatusRequestDTO;
import co.com.bookhive.catalog.services.input.dto.request.TitleRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.CatalogAdminResponseDTOs;
import co.com.bookhive.catalog.services.input.ports.ChangeCategoryStatusInputPort;
import co.com.bookhive.catalog.services.input.ports.FindTitleInputPort;
import co.com.bookhive.catalog.services.input.ports.ListCategoriesInputPort;
import co.com.bookhive.catalog.services.input.ports.RegisterTitleInputPort;
import co.com.bookhive.catalog.services.input.ports.SaveCategoryInputPort;
import co.com.bookhive.catalog.services.input.ports.UpdateTitleInputPort;
import co.com.bookhive.catalog.services.input.ports.WithdrawTitleInputPort;
import co.com.bookhive.catalog.services.usecase.catalogadmin.changecategorystatus.ChangeCategoryStatusUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.FindTitleUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.listcategories.ListCategoriesUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.registertitle.RegisterTitleUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.savecategory.SaveCategoryUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.updatetitle.UpdateTitleUseCase;
import co.com.bookhive.catalog.services.usecase.catalogadmin.withdrawtitle.WithdrawTitleUseCase;
import co.com.bookhive.crosscutting.helper.ObjectHelper;

public final class CatalogAdminInputAdapters {

	private static final TitleRequestDTO EMPTY_TITLE = new TitleRequestDTO(null, null, null, null, null, null, null, null, null, null);

	private CatalogAdminInputAdapters() {
		super();
	}

	public static ListCategoriesInputPort listCategories(final ListCategoriesUseCase useCase) {
		return () -> useCase.execute().stream().map(CatalogAdminInputAdapters::category).toList();
	}

	public static SaveCategoryInputPort saveCategory(final SaveCategoryUseCase useCase) {
		return request -> {
			final CategoryRequestDTO safe = ObjectHelper.getDefaultValue(request, new CategoryRequestDTO(null, null));
			return category(useCase.execute(new CatalogAdminDomains.Category(safe.id(), safe.name(), null, 0)));
		};
	}

	public static ChangeCategoryStatusInputPort changeCategoryStatus(final ChangeCategoryStatusUseCase useCase) {
		return request -> {
			final CategoryStatusRequestDTO safe = ObjectHelper.getDefaultValue(request, new CategoryStatusRequestDTO(null, true));
			return category(useCase.execute(new CatalogAdminDomains.CategoryStatusChange(safe.id(), safe.active())));
		};
	}

	public static FindTitleInputPort findTitle(final FindTitleUseCase useCase) {
		return id -> title(useCase.execute(id));
	}

	public static RegisterTitleInputPort registerTitle(final RegisterTitleUseCase useCase) {
		return request -> title(useCase.execute(domainOf(request)));
	}

	public static UpdateTitleInputPort updateTitle(final UpdateTitleUseCase useCase) {
		return request -> title(useCase.execute(domainOf(request)));
	}

	public static WithdrawTitleInputPort withdrawTitle(final WithdrawTitleUseCase useCase) {
		return id -> title(useCase.execute(id));
	}

	private static CatalogAdminDomains.Title domainOf(final TitleRequestDTO request) {
		final TitleRequestDTO safe = ObjectHelper.getDefaultValue(request, EMPTY_TITLE);
		return new CatalogAdminDomains.Title(safe.id(), safe.title(), safe.isbn(), ObjectHelper.getDefaultValue(safe.year(), 0),
				safe.format(), null, safe.category(), safe.publisher(), safe.authors(), safe.branch(), safe.barcodes());
	}

	private static CatalogAdminResponseDTOs.Category category(final CatalogAdminDomains.Category category) {
		return new CatalogAdminResponseDTOs.Category(category.id(), category.name(), category.status(), category.activeTitles());
	}

	private static CatalogAdminResponseDTOs.Title title(final CatalogAdminDomains.Title title) {
		return new CatalogAdminResponseDTOs.Title(title.id(), title.title(), title.isbn(), title.year(), title.format(),
				title.status(), title.category(), title.publisher(), title.authors());
	}

}
