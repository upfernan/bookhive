package co.com.bookhive.initializer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.com.bookhive.catalog.infrastructure.input.controllers.CatalogAdminController;
import co.com.bookhive.catalog.services.input.adapters.CatalogAdminInputAdapters;
import co.com.bookhive.catalog.services.output.ports.BookCopyInventoryPort;
import co.com.bookhive.catalog.services.output.ports.CatalogSessionPort;
import co.com.bookhive.catalog.services.output.ports.repositories.CategoryRepository;
import co.com.bookhive.catalog.services.output.ports.repositories.TitleRepository;
import co.com.bookhive.catalog.services.usecase.catalogadmin.changecategorystatus.impl.ChangeCategoryStatusUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.findtitle.impl.FindTitleUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.listcategories.impl.ListCategoriesUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.registertitle.impl.RegisterTitleUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.savecategory.impl.SaveCategoryUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.updatetitle.impl.UpdateTitleUseCaseImpl;
import co.com.bookhive.catalog.services.usecase.catalogadmin.withdrawtitle.impl.WithdrawTitleUseCaseImpl;
import co.com.bookhive.shared.services.output.ports.AuditPort;

@Configuration
public class CatalogAdminConfiguration {

	@Bean
	CatalogAdminController.Ports catalogAdminPorts(
			final CategoryRepository categories,
			final TitleRepository titles,
			final BookCopyInventoryPort inventory,
			final CatalogSessionPort session,
			final AuditPort audit) {

		return new CatalogAdminController.Ports(
				CatalogAdminInputAdapters.listCategories(new ListCategoriesUseCaseImpl(categories)),
				CatalogAdminInputAdapters.saveCategory(new SaveCategoryUseCaseImpl(categories, session, audit)),
				CatalogAdminInputAdapters.changeCategoryStatus(new ChangeCategoryStatusUseCaseImpl(categories, session, audit)),
				CatalogAdminInputAdapters.findTitle(new FindTitleUseCaseImpl(titles)),
				CatalogAdminInputAdapters.registerTitle(new RegisterTitleUseCaseImpl(titles, categories, inventory, session, audit)),
				CatalogAdminInputAdapters.updateTitle(new UpdateTitleUseCaseImpl(titles, categories, inventory, session, audit)),
				CatalogAdminInputAdapters.withdrawTitle(new WithdrawTitleUseCaseImpl(titles, inventory, session, audit)));
	}

}
