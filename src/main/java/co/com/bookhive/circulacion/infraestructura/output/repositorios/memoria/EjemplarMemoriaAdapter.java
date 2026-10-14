package co.com.bookhive.circulacion.infraestructura.output.repositorios.memoria;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.EjemplarEntidad;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.PrestamoEntidad;
import co.com.bookhive.circulacion.servicios.output.ports.EjemplarRepositorioPort;

// TEMPORAL: adaptador de salida de ejemplares sobre la base en memoria.
// Un ejemplar está disponible si su estado es DISPONIBLE y no tiene un
// préstamo activo. Así no hay que cambiarle el estado al prestarlo.
@Repository
public class EjemplarMemoriaAdapter implements EjemplarRepositorioPort {

	private final BaseDatosMemoria baseDatos;

	public EjemplarMemoriaAdapter(final BaseDatosMemoria baseDatos) {
		super();
		this.baseDatos = baseDatos;
	}

	@Override
	public boolean existe(final UUID ejemplar) {
		return baseDatos.existeEjemplar(ejemplar);
	}

	@Override
	public boolean estaDisponible(final UUID ejemplar) {
		final boolean estadoDisponible = EjemplarEntidad.ESTADO_DISPONIBLE
				.equals(baseDatos.buscarEjemplar(ejemplar).getEstado());

		final boolean tienePrestamoActivo = baseDatos.listarPrestamos().stream()
				.anyMatch(prestamo -> prestamo.getEjemplar().equals(ejemplar)
						&& PrestamoEntidad.ESTADO_ACTIVO.equals(prestamo.getEstado()));

		return estadoDisponible && !tienePrestamoActivo;
	}

	@Override
	public boolean esPrestable(final UUID ejemplar) {
		return baseDatos.buscarEjemplar(ejemplar).isPrestable();
	}

}
