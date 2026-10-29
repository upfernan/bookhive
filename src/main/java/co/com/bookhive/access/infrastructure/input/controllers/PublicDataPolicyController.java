package co.com.bookhive.access.infrastructure.input.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.access.services.input.dto.response.PublicDataPolicyResponseDTO;
import co.com.bookhive.access.services.input.ports.FindPublicDataPolicyInputPort;

@RestController
@RequestMapping(PublicDataPolicyController.PATH)
public class PublicDataPolicyController {

	public static final String PATH = "/api/v1/politica-datos/publica";

	private final FindPublicDataPolicyInputPort findPolicy;

	public PublicDataPolicyController(final FindPublicDataPolicyInputPort findPolicy) {
		super();
		this.findPolicy = findPolicy;
	}

	@GetMapping
	public ResponseEntity<PublicDataPolicyResponseDTO> find(
			@RequestParam(name = "dominio", required = false) final String domain) {
		return ResponseEntity.ok(findPolicy.execute(domain));
	}

}
