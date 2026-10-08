package co.com.bookhive.circulacion.servicios.output.ports;

import java.util.UUID;

public interface EjemplarRepositorioPort {

	boolean existe(UUID ejemplar);

	boolean estaDisponible(UUID ejemplar);

	boolean esPrestable(UUID ejemplar);

}
