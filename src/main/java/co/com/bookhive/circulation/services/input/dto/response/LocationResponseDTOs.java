package co.com.bookhive.circulation.services.input.dto.response;

import java.util.List;
import java.util.UUID;

public final class LocationResponseDTOs {

	private LocationResponseDTOs() {
		super();
	}

	public record Level(int depth, String name) {
	}

	public record Branch(UUID id, String name) {
	}

	public record Spot(int depth, String level, String value) {
	}

	public record BookCopyLocation(UUID bookCopy, String barcode, String status, Branch branch, List<Spot> spots, String description) {
	}

}
