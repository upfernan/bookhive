package co.com.bookhive.catalog.infrastructure.input.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import co.com.bookhive.catalog.services.input.dto.response.SyncAllCatalogsResponseDTO;
import co.com.bookhive.catalog.services.input.ports.SyncAllCatalogsInputPort;
import co.com.bookhive.crosscutting.helper.TextHelper;

@Component
public class CatalogSyncOnStartup {

	private static final Logger logger = LoggerFactory.getLogger(CatalogSyncOnStartup.class);

	private final SyncAllCatalogsInputPort syncAllCatalogsInputPort;
	private final boolean enabled;

	public CatalogSyncOnStartup(
			final SyncAllCatalogsInputPort syncAllCatalogsInputPort,
			@Value("${bookhive.search.sync-on-startup:true}") final boolean syncOnStartup,
			@Value("${bookhive.search.url:}") final String searchUrl) {

		super();
		this.syncAllCatalogsInputPort = syncAllCatalogsInputPort;
		this.enabled = syncOnStartup && !TextHelper.isDefaultValue(searchUrl);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		if (!enabled) {
			logger.info("La sincronización del catálogo al arrancar está apagada o no hay buscador configurado.");
			return;
		}
		Thread.ofVirtual().name("sincronizar-catalogos").start(this::syncAllCatalogs);
	}

	void syncAllCatalogs() {
		try {
			final SyncAllCatalogsResponseDTO result = syncAllCatalogsInputPort.execute();
			logger.info("Catálogos llevados al buscador: {}.", result.synced());
			if (!result.failed().isEmpty()) {
				logger.warn("Catálogos que no se pudieron llevar al buscador: {}.", result.failed());
			}
		} catch (final RuntimeException exception) {
			logger.warn("No se pudo sincronizar los catálogos al arrancar; se reintentará en el próximo arranque.", exception);
		}
	}

	boolean isEnabled() {
		return enabled;
	}

}
