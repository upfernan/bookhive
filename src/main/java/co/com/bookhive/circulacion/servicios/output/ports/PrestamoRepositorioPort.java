package co.com.bookhive.circulacion.servicios.output.ports;

import java.util.UUID;

public interface PrestamoRepositorioPort {

	int contarActivosPorUsuario(UUID usuario);

}
