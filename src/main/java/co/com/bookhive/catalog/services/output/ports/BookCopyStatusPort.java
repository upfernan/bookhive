package co.com.bookhive.catalog.services.output.ports;

import co.com.bookhive.catalog.services.output.ports.entities.BookCopyStatusEntity;

public interface BookCopyStatusPort {

	BookCopyStatusEntity findByBarcode(String barcode);

}
