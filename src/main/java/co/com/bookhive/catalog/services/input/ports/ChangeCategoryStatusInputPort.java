package co.com.bookhive.catalog.services.input.ports;

import co.com.bookhive.catalog.services.input.dto.request.CategoryStatusRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.CatalogAdminResponseDTOs;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface ChangeCategoryStatusInputPort extends InputPortWithReturn<CategoryStatusRequestDTO, CatalogAdminResponseDTOs.Category> {

}
