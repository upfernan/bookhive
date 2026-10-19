package co.com.bookhive.circulation.services.output.ports.repositories;

import java.util.UUID;

public interface PermissionRepository {

	boolean isGranted(UUID person, String permission);

}
