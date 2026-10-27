package co.com.bookhive.catalog.services.input.ports;

import co.com.bookhive.catalog.services.input.dto.request.CategoryRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.CatalogAdminResponseDTOs;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface SaveCategoryInputPort extends InputPortWithReturn<CategoryRequestDTO, CatalogAdminResponseDTOs.Category> {

}
