package co.com.bookhive.shared.services.output.ports;

import java.util.UUID;

public interface AuditPort {

	String LOAN_REGISTERED = "PRESTAMO_REGISTRADO";
	String PERMISSION_DENIED = "PERMISO_DENEGADO";

	void record(String action, UUID institution, UUID person, String detail);

}
