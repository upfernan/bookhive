package co.com.bookhive.circulacion.servicios.output.ports;

import java.util.UUID;

public interface MultaRepositorioPort {

	boolean tienePendientes(UUID usuario);

}
