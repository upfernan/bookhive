package co.com.bookhive.circulation.services.input.dto.request;

import java.util.List;
import java.util.UUID;

public record RelocateBookCopyRequestDTO(String barcode, UUID branch, List<String> values) {

	public RelocateBookCopyRequestDTO forBarcode(final String code) {
		return new RelocateBookCopyRequestDTO(code, branch, values);
	}

}
