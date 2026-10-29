package co.com.bookhive.catalog.domain.bookcopy.findbookcopydetails;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FindBookCopyDetailsDomainTest {

	@Test
	@DisplayName("guarda el código sin espacios y lo acepta")
	void acceptsBarcode() {
		final FindBookCopyDetailsDomain search = new FindBookCopyDetailsDomain("  UCO-0001 ");

		assertEquals("UCO-0001", search.barcode());
		assertDoesNotThrow(search::validate);
	}

	@Test
	@DisplayName("sin código pide que se indique")
	void requiresBarcode() {
		final FindBookCopyDetailsDomain search = new FindBookCopyDetailsDomain(null);

		final BookHiveException exception = assertThrows(BookHiveException.class, search::validate);

		assertEquals(MessageKey.BOOK_COPY_BARCODE_REQUIRED, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.MISSING_DATA, exception.getKind());
	}

}
