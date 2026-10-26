package co.com.bookhive.circulation.services.output.ports.repositories.entities;

import java.util.UUID;

public final class LocationEntities {

	private LocationEntities() {
		super();
	}

	public record BranchEntity(UUID id, String name) {
	}

	public record LevelEntity(int depth, String name) {
	}

	public record ValueEntity(int depth, String value) {
	}

}
