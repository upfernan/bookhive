package co.com.bookhive.access.services.output.ports.repositories.entities;

import java.time.Instant;
import java.util.UUID;

public final class RegistrationEntities {

	private RegistrationEntities() {
		super();
	}

	public record DomainInstitution(UUID institution, String schema) {
	}

	public record PublicPolicy(int version, String content, Instant publishedAt) {
	}

	public record NewPerson(UUID id, String identity, String name, String email, int policyVersion) {
	}

}
