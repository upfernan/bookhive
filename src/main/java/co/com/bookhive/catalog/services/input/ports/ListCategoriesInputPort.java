package co.com.bookhive.catalog.services.input.ports;

import java.util.List;
import co.com.bookhive.catalog.services.input.dto.response.CatalogAdminResponseDTOs;

public interface ListCategoriesInputPort {

	List<CatalogAdminResponseDTOs.Category> execute();

}
