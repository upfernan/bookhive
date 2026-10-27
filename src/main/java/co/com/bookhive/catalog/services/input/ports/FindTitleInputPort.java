package co.com.bookhive.catalog.services.input.ports;

import java.util.UUID;

import co.com.bookhive.catalog.services.input.dto.response.CatalogAdminResponseDTOs;
import co.com.bookhive.shared.services.input.ports.InputPortWithReturn;

public interface FindTitleInputPort extends InputPortWithReturn<UUID, CatalogAdminResponseDTOs.Title> {

}
