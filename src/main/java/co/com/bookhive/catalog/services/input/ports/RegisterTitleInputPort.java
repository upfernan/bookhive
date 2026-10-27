package co.com.bookhive.catalog.services.input.ports;

import co.com.bookhive.catalog.services.input.dto.request.TitleRequestDTO;
import co.com.bookhive.catalog.services.input.dto.response.CatalogAdminResponseDTOs;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface RegisterTitleInputPort extends InputPortWithReturn<TitleRequestDTO, CatalogAdminResponseDTOs.Title> {

}
