package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.UUID;

public interface InstitutionRepository {

	String findActiveSchema(UUID institution);

}
