package co.com.bookhive.catalog.infrastructure.input.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.catalog.services.input.dto.response.MyAccountResponseDTO;
import co.com.bookhive.catalog.services.input.ports.ViewMyAccountInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
@RequestMapping("/api/v1/mi-cuenta")
public class MyAccountController {

	private static final Logger logger = LoggerFactory.getLogger(MyAccountController.class);

	private final ViewMyAccountInputPort viewMyAccount;

	public MyAccountController(final ViewMyAccountInputPort viewMyAccount) {
		super();
		this.viewMyAccount = viewMyAccount;
	}

	@RequiredPermission(PermissionCode.VIEW_ACCOUNT)
	@Transactional(readOnly = true)
	@GetMapping
	public ResponseEntity<MyAccountResponseDTO> view() {
		final MyAccountResponseDTO account = viewMyAccount.execute();
		final int loans = account.activeLoans().size();
		final int fines = account.pendingFines().size();
		logger.info("Panel del usuario consultado: {} préstamo(s) activo(s) y {} multa(s) pendiente(s).", loans, fines);
		return ResponseEntity.ok(account);
	}

}
