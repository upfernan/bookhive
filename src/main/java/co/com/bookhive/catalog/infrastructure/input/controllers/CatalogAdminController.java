package co.com.bookhive.catalog.infrastructure.input.controllers;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
public class CatalogAdminController {

	private static final String CATEGORIES = "/api/v1/admin/categorias";
	private static final String TITLES = "/api/v1/catalogo/titulos";

	private static final Logger logger = LoggerFactory.getLogger(CatalogAdminController.class);

	public record Ports(
			ListCategoriesInputPort listCategories,
			SaveCategoryInputPort saveCategory,
			ChangeCategoryStatusInputPort changeCategoryStatus,
			FindTitleInputPort findTitle,
			RegisterTitleInputPort registerTitle,
			UpdateTitleInputPort updateTitle,
			WithdrawTitleInputPort withdrawTitle) {
	}

	private final Ports ports;

	public CatalogAdminController(final Ports ports) {
		super();
		this.ports = ports;
	}

	@RequiredPermission(PermissionCode.SEARCH_CATALOG)
	@Transactional(readOnly = true)
	@GetMapping("/api/v1/categorias")
	public ResponseEntity<List<CatalogAdminResponseDTOs.Category>> categories() {
		return ResponseEntity.ok(ports.listCategories().execute());
	}

	@RequiredPermission(PermissionCode.MANAGE_CATEGORIES)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping(CATEGORIES)
	public ResponseEntity<CatalogAdminResponseDTOs.Category> createCategory(@RequestBody final CategoryRequestDTO request) {
		final CatalogAdminResponseDTOs.Category category = ports.saveCategory().execute(safe(request).forId(null));
		logger.info("Se creó una categoría.");
		return new ResponseEntity<>(category, HttpStatus.CREATED);
	}

	@RequiredPermission(PermissionCode.MANAGE_CATEGORIES)
	@Transactional(rollbackFor = Exception.class)
	@PutMapping(CATEGORIES + "/{id}")
	public ResponseEntity<CatalogAdminResponseDTOs.Category> renameCategory(
			@PathVariable final UUID id, @RequestBody final CategoryRequestDTO request) {
		return ResponseEntity.ok(ports.saveCategory().execute(safe(request).forId(id)));
	}

	@RequiredPermission(PermissionCode.MANAGE_CATEGORIES)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping(CATEGORIES + "/{id}/desactivacion")
	public ResponseEntity<CatalogAdminResponseDTOs.Category> deactivateCategory(@PathVariable final UUID id) {
		return ResponseEntity.ok(ports.changeCategoryStatus().execute(new CategoryStatusRequestDTO(id, false)));
	}

	@RequiredPermission(PermissionCode.MANAGE_CATEGORIES)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping(CATEGORIES + "/{id}/activacion")
	public ResponseEntity<CatalogAdminResponseDTOs.Category> activateCategory(@PathVariable final UUID id) {
		return ResponseEntity.ok(ports.changeCategoryStatus().execute(new CategoryStatusRequestDTO(id, true)));
	}

	@RequiredPermission(PermissionCode.MANAGE_CATALOG)
	@Transactional(readOnly = true)
	@GetMapping(TITLES + "/{id}")
	public ResponseEntity<CatalogAdminResponseDTOs.Title> title(@PathVariable final UUID id) {
		return ResponseEntity.ok(ports.findTitle().execute(id));
	}

	@RequiredPermission(PermissionCode.MANAGE_CATALOG)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping(TITLES)
	public ResponseEntity<CatalogAdminResponseDTOs.Title> registerTitle(@RequestBody final TitleRequestDTO request) {
		final CatalogAdminResponseDTOs.Title title = ports.registerTitle().execute(safe(request).forId(null));
		logger.info("Se registró un título en el catálogo.");
		return new ResponseEntity<>(title, HttpStatus.CREATED);
	}

	@RequiredPermission(PermissionCode.MANAGE_CATALOG)
	@Transactional(rollbackFor = Exception.class)
	@PutMapping(TITLES + "/{id}")
	public ResponseEntity<CatalogAdminResponseDTOs.Title> updateTitle(
			@PathVariable final UUID id, @RequestBody final TitleRequestDTO request) {
		return ResponseEntity.ok(ports.updateTitle().execute(safe(request).forId(id)));
	}

	@RequiredPermission(PermissionCode.MANAGE_CATALOG)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping(TITLES + "/{id}/baja")
	public ResponseEntity<CatalogAdminResponseDTOs.Title> withdrawTitle(@PathVariable final UUID id) {
		final CatalogAdminResponseDTOs.Title title = ports.withdrawTitle().execute(id);
		logger.info("Se dio de baja un título del catálogo.");
		return ResponseEntity.ok(title);
	}

	private static CategoryRequestDTO safe(final CategoryRequestDTO request) {
		return request == null ? new CategoryRequestDTO(null, null) : request;
	}

	private static TitleRequestDTO safe(final TitleRequestDTO request) {
		return request == null ? new TitleRequestDTO(null, null, null, null, null, null, null, null, null, null) : request;
	}

}
