package co.com.bookhive.catalog.infrastructure.output.repositories.structured.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.CatalogChangeJpaRepository;
import co.com.bookhive.catalog.infrastructure.output.repositories.structured.jpa.entities.CatalogChangeJpaEntity;
import co.com.bookhive.catalog.services.output.ports.repositories.criteria.CatalogChangeCriteria;
import co.com.bookhive.catalog.services.output.ports.repositories.entities.CatalogChangeEntity;

class CatalogChangeRepositoryAdapterTest {

	private final List<String> calls = new ArrayList<>();
	private final CatalogChangeJpaEntity change = new CatalogChangeJpaEntity();

	@Test
	@DisplayName("trae los cambios pendientes con el límite pedido")
	void findsPendingChanges() {
		change.setId(UUID.randomUUID());
		change.setBook(UUID.randomUUID());

		final List<CatalogChangeEntity> pending = adapter().findByCriteria(CatalogChangeCriteria.pending(25));

		assertEquals(List.of("pendientes:25"), calls);
		assertEquals(new CatalogChangeEntity(change.getId(), change.getBook()), pending.get(0));
	}

	@Test
	@DisplayName("sin criterio usa el límite máximo")
	void usesMaxLimitWithoutCriteria() {
		adapter().findByCriteria(null);

		assertEquals(List.of("pendientes:" + CatalogChangeCriteria.MAX_LIMIT), calls);
	}

	@Test
	@DisplayName("marca como enviados los cambios indicados y no consulta si no hay ninguno")
	void marksSent() {
		final UUID sent = UUID.randomUUID();
		final CatalogChangeRepositoryAdapter adapter = adapter();

		adapter.markSent(List.of());
		adapter.markSent(null);
		assertTrue(calls.isEmpty());

		adapter.markSent(List.of(sent));
		assertEquals(List.of("enviados:[" + sent + "]"), calls);
	}

	private CatalogChangeRepositoryAdapter adapter() {
		final CatalogChangeJpaRepository repository = (CatalogChangeJpaRepository) Proxy.newProxyInstance(
				CatalogChangeJpaRepository.class.getClassLoader(),
				new Class<?>[] { CatalogChangeJpaRepository.class },
				(proxy, method, arguments) -> switch (method.getName()) {
					case "findPending" -> {
						calls.add("pendientes:" + arguments[0]);
						yield List.of(change);
					}
					case "markSent" -> {
						calls.add("enviados:" + List.copyOf((Collection<?>) arguments[0]));
						yield 1;
					}
					default -> throw new UnsupportedOperationException(method.getName());
				});
		return new CatalogChangeRepositoryAdapter(repository);
	}

}
