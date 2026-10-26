package co.com.bookhive.payment.infrastructure.output.externalservices.tenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import co.com.bookhive.crosscutting.tenancy.TenantContext;

class TenantTransactionScopeAdapterTest {

	private final List<String> steps = new ArrayList<>();

	@AfterEach
	void clearTenant() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("ejecuta la acción dentro de la institución y de una transacción, y vuelve a como estaba")
	void runsInsideInstitutionAndTransaction() {
		final String result = new TenantTransactionScopeAdapter(new RecordingTransactions()).within("uco", () -> {
			steps.add("acción en " + TenantContext.get());
			return "hecho";
		});

		assertEquals("hecho", result);
		assertEquals(List.of("abre en uco", "acción en uco", "confirma"), steps);
		assertEquals(TenantContext.SCHEMA_WITHOUT_TENANT, TenantContext.get());
	}

	@Test
	@DisplayName("si la acción falla deshace la transacción y devuelve la institución anterior")
	void rollsBackAndRestoresPreviousInstitution() {
		TenantContext.set("eafit");
		final TenantTransactionScopeAdapter scope = new TenantTransactionScopeAdapter(new RecordingTransactions());

		assertThrows(IllegalStateException.class, () -> scope.within("uco", () -> {
			throw new IllegalStateException("falla");
		}));

		assertEquals(List.of("abre en uco", "deshace"), steps);
		assertEquals("eafit", TenantContext.get());
	}

	private final class RecordingTransactions implements PlatformTransactionManager {

		@Override
		public TransactionStatus getTransaction(final TransactionDefinition definition) {
			steps.add("abre en " + TenantContext.get());
			return new SimpleTransactionStatus();
		}

		@Override
		public void commit(final TransactionStatus status) {
			steps.add("confirma");
		}

		@Override
		public void rollback(final TransactionStatus status) {
			steps.add("deshace");
		}

	}

}
