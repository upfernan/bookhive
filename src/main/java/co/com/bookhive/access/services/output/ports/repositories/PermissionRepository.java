package co.com.bookhive.access.services.output.ports.repositories;

import java.util.UUID;

public interface PermissionRepository {

	boolean isGranted(UUID person, String permission);

}
