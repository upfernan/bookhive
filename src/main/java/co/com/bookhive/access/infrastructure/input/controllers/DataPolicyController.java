package co.com.bookhive.access.infrastructure.input.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.access.services.input.dto.request.AcceptDataPolicyRequestDTO;
import co.com.bookhive.access.services.input.dto.response.DataPolicyResponseDTO;
import co.com.bookhive.access.services.input.ports.AcceptDataPolicyInputPort;
import co.com.bookhive.access.services.input.ports.FindDataPolicyInputPort;

@RestController
@RequestMapping(DataPolicyController.PATH)
public class DataPolicyController {

	public static final String PATH = "/api/v1/politica-datos";
	public static final String ACCEPTANCE = "/aceptacion";

	private static final Logger logger = LoggerFactory.getLogger(DataPolicyController.class);

	private final FindDataPolicyInputPort findDataPolicy;
	private final AcceptDataPolicyInputPort acceptDataPolicy;

	public DataPolicyController(
			final FindDataPolicyInputPort findDataPolicy,
			final AcceptDataPolicyInputPort acceptDataPolicy) {

		super();
		this.findDataPolicy = findDataPolicy;
		this.acceptDataPolicy = acceptDataPolicy;
	}

	@GetMapping
	public ResponseEntity<DataPolicyResponseDTO> find() {
		return ResponseEntity.ok(findDataPolicy.execute());
	}

	@PostMapping(ACCEPTANCE)
	public ResponseEntity<DataPolicyResponseDTO> accept(@RequestBody(required = false) final AcceptDataPolicyRequestDTO request) {
		final DataPolicyResponseDTO response = acceptDataPolicy.execute(request);
		final int version = response.version();
		logger.info("Quedó registrada la aceptación de la política de datos, versión {}.", version);
		return ResponseEntity.ok(response);
	}

}
