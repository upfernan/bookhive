package co.com.bookhive.catalog.infrastructure.input.listeners;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.catalog.services.input.dto.response.SyncAllCatalogsResponseDTO;
import co.com.bookhive.catalog.services.input.ports.SyncAllCatalogsInputPort;

class CatalogSyncOnStartupTest {

	@Test
	@DisplayName("al arrancar lleva los catálogos al buscador en un hilo aparte")
	void syncsInBackgroundOnStartup() throws InterruptedException {
		final CountDownLatch done = new CountDownLatch(1);
		final SyncAllCatalogsInputPort port = () -> {
			done.countDown();
			return new SyncAllCatalogsResponseDTO(List.of("uco"), List.of("unal: caído"));
		};
		final CatalogSyncOnStartup listener = new CatalogSyncOnStartup(port, true, "http://10.142.0.2:8108");

		listener.onApplicationReady();

		assertTrue(listener.isEnabled());
		assertTrue(done.await(5, TimeUnit.SECONDS));
	}

	@Test
	@DisplayName("no sincroniza si está apagado o si no hay buscador configurado")
	void skipsWhenDisabled() {
		final AtomicInteger calls = new AtomicInteger();
		final SyncAllCatalogsInputPort port = () -> {
			calls.incrementAndGet();
			return new SyncAllCatalogsResponseDTO(List.of(), List.of());
		};
		final CatalogSyncOnStartup turnedOff = new CatalogSyncOnStartup(port, false, "http://10.142.0.2:8108");
		final CatalogSyncOnStartup withoutSearch = new CatalogSyncOnStartup(port, true, " ");

		turnedOff.onApplicationReady();
		withoutSearch.onApplicationReady();

		assertFalse(turnedOff.isEnabled());
		assertFalse(withoutSearch.isEnabled());
		assertEquals(0, calls.get());
	}

	@Test
	@DisplayName("si la sincronización falla no tumba el arranque")
	void survivesFailures() {
		final CatalogSyncOnStartup listener = new CatalogSyncOnStartup(() -> {
			throw new IllegalStateException("la base no responde");
		}, true, "http://10.142.0.2:8108");

		assertDoesNotThrow(listener::syncAllCatalogs);
	}

}
