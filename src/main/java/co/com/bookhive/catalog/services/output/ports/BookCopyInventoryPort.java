package co.com.bookhive.catalog.services.output.ports;

import java.util.List;
import java.util.UUID;

public interface BookCopyInventoryPort {

	int register(UUID book, UUID branch, List<String> barcodes);

	int retireAll(UUID book);

}
