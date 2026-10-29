package co.com.bookhive.circulation.infrastructure.input.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.circulation.services.input.dto.request.InstitutionSettingsRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.InstitutionSettingsResponseDTO;
import co.com.bookhive.circulation.services.input.ports.ConfigureInstitutionInputPort;
import co.com.bookhive.circulation.services.input.ports.FindInstitutionSettingsInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
@RequestMapping("/api/v1/admin/parametros")
public class InstitutionSettingsController {

	private static final Logger logger = LoggerFactory.getLogger(InstitutionSettingsController.class);

	private final FindInstitutionSettingsInputPort findSettings;
	private final ConfigureInstitutionInputPort configure;

	public InstitutionSettingsController(
			final FindInstitutionSettingsInputPort findSettings,
			final ConfigureInstitutionInputPort configure) {

		super();
		this.findSettings = findSettings;
		this.configure = configure;
	}

	@RequiredPermission(PermissionCode.CONFIGURE_INSTITUTION)
	@Transactional(readOnly = true)
	@GetMapping
	public ResponseEntity<InstitutionSettingsResponseDTO> find() {
		return ResponseEntity.ok(findSettings.execute());
	}

	@RequiredPermission(PermissionCode.CONFIGURE_INSTITUTION)
	@Transactional(rollbackFor = Exception.class)
	@PutMapping
	public ResponseEntity<InstitutionSettingsResponseDTO> update(@RequestBody final InstitutionSettingsRequestDTO request) {
		logger.info("Inicia el cambio de las reglas de préstamo y multas de la institución.");
		final InstitutionSettingsResponseDTO response = configure.execute(request);
		logger.info("Reglas de la institución actualizadas.");
		return ResponseEntity.ok(response);
	}

}
