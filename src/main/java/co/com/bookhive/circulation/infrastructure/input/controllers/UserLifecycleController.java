package co.com.bookhive.circulation.infrastructure.input.controllers;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import co.com.bookhive.circulation.services.input.dto.request.UserLifecycleRequestDTO;
import co.com.bookhive.circulation.services.input.dto.response.UserLifecycleResponseDTOs;
import co.com.bookhive.circulation.services.input.ports.ChangeUserStatusInputPort;
import co.com.bookhive.circulation.services.input.ports.ConfigureRetentionInputPort;
import co.com.bookhive.circulation.services.input.ports.CurrentUserInputPort;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.security.RequiredPermission;

@RestController
public class UserLifecycleController {

	private static final String USERS = "/api/v1/admin/usuarios";

	private static final Logger logger = LoggerFactory.getLogger(UserLifecycleController.class);

	private final ChangeUserStatusInputPort changeStatus;
	private final ConfigureRetentionInputPort retention;
	private final CurrentUserInputPort currentUser;

	public UserLifecycleController(
			final ChangeUserStatusInputPort changeStatus,
			final ConfigureRetentionInputPort retention,
			final CurrentUserInputPort currentUser) {
		super();
		this.changeStatus = changeStatus;
		this.retention = retention;
		this.currentUser = currentUser;
	}

	@RequiredPermission(PermissionCode.MANAGE_USERS)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping(USERS + "/{id}/baja")
	public ResponseEntity<UserLifecycleResponseDTOs.Account> deactivate(@PathVariable final UUID id) {
		return change(id, UserLifecycleRequestDTO.DEACTIVATE);
	}

	@RequiredPermission(PermissionCode.MANAGE_USERS)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping(USERS + "/{id}/reactivacion")
	public ResponseEntity<UserLifecycleResponseDTOs.Account> reactivate(@PathVariable final UUID id) {
		return change(id, UserLifecycleRequestDTO.REACTIVATE);
	}

	@RequiredPermission(PermissionCode.MANAGE_USERS)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping(USERS + "/{id}/depuracion")
	public ResponseEntity<UserLifecycleResponseDTOs.Account> purge(@PathVariable final UUID id) {
		return change(id, UserLifecycleRequestDTO.PURGE);
	}

	@RequiredPermission(PermissionCode.VIEW_ACCOUNT)
	@Transactional(rollbackFor = Exception.class)
	@PostMapping("/api/v1/mi-cuenta/depuracion")
	public ResponseEntity<UserLifecycleResponseDTOs.Account> purgeMine() {
		return change(currentUser.execute(), UserLifecycleRequestDTO.PURGE);
	}

	@RequiredPermission(PermissionCode.CONFIGURE_INSTITUTION)
	@Transactional(readOnly = true)
	@GetMapping(USERS + "/conservacion")
	public ResponseEntity<UserLifecycleResponseDTOs.Retention> retention() {
		return ResponseEntity.ok(retention.execute(null));
	}

	@RequiredPermission(PermissionCode.CONFIGURE_INSTITUTION)
	@Transactional(rollbackFor = Exception.class)
	@PutMapping(USERS + "/conservacion")
	public ResponseEntity<UserLifecycleResponseDTOs.Retention> changeRetention(
			@RequestBody final UserLifecycleResponseDTOs.Retention request) {
		return ResponseEntity.ok(retention.execute(request == null ? new UserLifecycleResponseDTOs.Retention(0) : request));
	}

	private ResponseEntity<UserLifecycleResponseDTOs.Account> change(final UUID id, final String change) {
		final UserLifecycleResponseDTOs.Account account = changeStatus.execute(new UserLifecycleRequestDTO(id, change));
		final String status = account.status();
		logger.info("Cuenta de usuario cambiada ({}): queda {}.", change, status);
		return ResponseEntity.ok(account);
	}

}
