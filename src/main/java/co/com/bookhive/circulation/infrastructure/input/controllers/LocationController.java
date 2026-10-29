package co.com.bookhive.circulation.infrastructure.input.controllers;

import java.util.List;
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
import co.com.bookhive.circulation.services.input.dto.request.BranchRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.LocationLevelsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.request.RelocateBookCopyRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.LocationResponseDTOs;
import co.com.bookhive.circulation.services.input.ports.ConfigureLocationLevelsInputPort;
import co.com.bookhive.circulation.services.input.ports.CreateBranchInputPort;
import co.com.bookhive.circulation.services.input.ports.FindBookCopyLocationInputPort;
import co.com.bookhive.circulation.services.input.ports.FindLocationLevelsInputPort;
import co.com.bookhive.circulation.services.input.ports.ListBranchesInputPort;
import co.com.bookhive.circulation.services.input.ports.RelocateBookCopyInputPort;
import co.com.bookhive.circulation.services.input.ports.WithdrawBookCopyInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
public class LocationController {

	public static final String BOOK_COPY_LOCATION = "/api/v1/ejemplares/{barcode}/ubicacion";

	private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

	public record Ports(
			FindLocationLevelsInputPort findLevels,
			ConfigureLocationLevelsInputPort configureLevels,
			ListBranchesInputPort listBranches,
			CreateBranchInputPort createBranch,
			FindBookCopyLocationInputPort findLocation,
			RelocateBookCopyInputPort relocate,
			WithdrawBookCopyInputPort withdraw) {
	}

	private final Ports ports;

	public LocationController(final Ports ports) {
		super();
		this.ports = ports;
	}

	@RequiredPermission(PermissionCode.LOCATE_BOOK_COPY)
	@Transactional(readOnly = true)
	@GetMapping("/api/v1/ubicacion/niveles")
	public ResponseEntity<List<LocationResponseDTOs.Level>> levels() {
		return ResponseEntity.ok(ports.findLevels().execute());
	}

	@RequiredPermission(PermissionCode.CONFIGURE_INSTITUTION)
	@Transactional(rollbackFor = Exception.class)
	@PutMapping("/api/v1/admin/ubicacion/niveles")
	public ResponseEntity<List<LocationResponseDTOs.Level>> configureLevels(@RequestBody final LocationLevelsRequestDTO request) {
		final List<LocationResponseDTOs.Level> levels = ports.configureLevels().execute(request);
		final int count = levels.size();
		logger.info("La institución quedó con {} nivel(es) de ubicación.", count);
		return ResponseEntity.ok(levels);
	}

	@RequiredPermission(PermissionCode.LOCATE_BOOK_COPY)
	@Transactional(readOnly = true)
	@GetMapping("/api/v1/sedes")
	public ResponseEntity<List<LocationResponseDTOs.Branch>> branches() {
		return ResponseEntity.ok(ports.listBranches().execute());
	}

	@RequiredPermission(PermissionCode.CONFIGURE_INSTITUTION)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping("/api/v1/admin/sedes")
	public ResponseEntity<LocationResponseDTOs.Branch> createBranch(@RequestBody final BranchRequestDTO request) {
		final LocationResponseDTOs.Branch branch = ports.createBranch().execute(request);
		logger.info("Se creó una sede nueva.");
		return new ResponseEntity<>(branch, HttpStatus.CREATED);
	}

	@RequiredPermission(PermissionCode.LOCATE_BOOK_COPY)
	@Transactional(readOnly = true)
	@GetMapping(BOOK_COPY_LOCATION)
	public ResponseEntity<LocationResponseDTOs.BookCopyLocation> location(@PathVariable final String barcode) {
		return ResponseEntity.ok(ports.findLocation().execute(barcode));
	}

	@RequiredPermission(PermissionCode.RELOCATE_BOOK_COPY)
	@Transactional(rollbackFor = Exception.class)
	@PutMapping(BOOK_COPY_LOCATION)
	public ResponseEntity<LocationResponseDTOs.BookCopyLocation> relocate(
			@PathVariable final String barcode, @RequestBody final RelocateBookCopyRequestDTO request) {
		final RelocateBookCopyRequestDTO safe = request == null ? new RelocateBookCopyRequestDTO(barcode, null, null) : request;
		final LocationResponseDTOs.BookCopyLocation location = ports.relocate().execute(safe.forBarcode(barcode));
		logger.info("Ejemplar reubicado.");
		return ResponseEntity.ok(location);
	}

	@RequiredPermission(PermissionCode.WITHDRAW_BOOK_COPY)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping("/api/v1/ejemplares/{barcode}/baja")
	public ResponseEntity<LocationResponseDTOs.BookCopyLocation> withdraw(@PathVariable final String barcode) {
		final LocationResponseDTOs.BookCopyLocation location = ports.withdraw().execute(barcode);
		logger.info("Ejemplar dado de baja.");
		return ResponseEntity.ok(location);
	}

}
