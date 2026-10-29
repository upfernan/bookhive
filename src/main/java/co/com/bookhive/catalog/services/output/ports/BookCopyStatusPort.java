package co.com.bookhive.catalog.services.output.ports;

import java.util.UUID;
import co.com.bookhive.catalog.services.output.ports.entities.BookCopyStatusEntity;

public interface BookCopyStatusPort {

	BookCopyStatusEntity findByBarcode(String barcode, UUID user);

}
