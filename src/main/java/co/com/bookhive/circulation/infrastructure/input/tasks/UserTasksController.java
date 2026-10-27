package co.com.bookhive.circulation.infrastructure.input.tasks;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.circulation.services.input.dto.response.UserLifecycleResponseDTOs;
import co.com.bookhive.circulation.services.input.ports.ListTaskInstitutionsInputPort;
import co.com.bookhive.circulation.services.input.ports.PurgeExpiredUsersInputPort;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

@RestController
public class UserTasksController {

	private static final Logger logger = LoggerFactory.getLogger(UserTasksController.class);

	private final ListTaskInstitutionsInputPort institutions;
	private final PurgeExpiredUsersInputPort purgeExpired;
	private final TransactionTemplate transactions;

	public UserTasksController(
			final ListTaskInstitutionsInputPort institutions,
			final PurgeExpiredUsersInputPort purgeExpired,
			final PlatformTransactionManager transactionManager) {
		super();
		this.institutions = institutions;
		this.purgeExpired = purgeExpired;
		this.transactions = new TransactionTemplate(transactionManager);
	}

	@PostMapping("/api/v1/tareas/depurar-usuarios")
	public ResponseEntity<UserLifecycleResponseDTOs.PurgeRuns> purgeExpired() {
		final List<UserLifecycleResponseDTOs.PurgeRun> runs = new ArrayList<>();
		for (final String institution : institutions.execute()) {
			TenantContext.set(institution);
			try {
				runs.add(transactions.execute(status -> purgeExpired.execute(institution)));
			} catch (final BookHiveException exception) {
				logger.error("Falló la depuración de usuarios de {}: {}", institution, exception.getTechnicalMessage());
			} finally {
				TenantContext.clear();
			}
		}
		final int count = runs.size();
		logger.info("Depuración de usuarios vencidos revisada en {} instituciones.", count);
		return ResponseEntity.ok(new UserLifecycleResponseDTOs.PurgeRuns(runs));
	}

}
