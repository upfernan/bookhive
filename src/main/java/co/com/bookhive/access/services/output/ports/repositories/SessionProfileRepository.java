package co.com.bookhive.access.services.output.ports.repositories;

import java.util.UUID;

import co.com.bookhive.access.services.output.ports.repositories.entities.SessionProfileEntity;

public interface SessionProfileRepository {

	SessionProfileEntity findProfile(UUID person, UUID institution);

}
