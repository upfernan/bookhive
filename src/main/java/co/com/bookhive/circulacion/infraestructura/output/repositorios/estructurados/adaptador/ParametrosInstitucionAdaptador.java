package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.adaptador;

import org.springframework.stereotype.Repository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.ParametrosInstitucionEntidad;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.jpa.ParametrosInstitucionJpaRepository;
import co.com.bookhive.circulacion.servicios.output.ports.ParametrosInstitucionPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;

// Los parámetros que cada institución ajusta por su cuenta (RN-10). Salen del
// esquema al que está conectada la petición, así que no hace falta preguntar
// de qué universidad son.
@Repository
public class ParametrosInstitucionAdaptador implements ParametrosInstitucionPort {

	private final ParametrosInstitucionJpaRepository repositorio;

	public ParametrosInstitucionAdaptador(final ParametrosInstitucionJpaRepository repositorio) {
		super();
		this.repositorio = repositorio;
	}

	@Override
	public int obtenerDiasDePrestamo() {
		return obtenerParametros().getDiasPrestamo();
	}

	@Override
	public int obtenerMaximoPrestamosSimultaneos() {
		return obtenerParametros().getMaximoPrestamosSimultaneos();
	}

	// Si la institución no tiene parámetros, se detiene con un mensaje claro en
	// vez de prestar con valores inventados. Una institución a medio configurar
	// no presta libros.
	private ParametrosInstitucionEntidad obtenerParametros() {
		return repositorio.findAll().stream()
				.findFirst()
				.orElseThrow(() -> BookHiveException.crear(
						"La institución todavía no tiene configuradas sus reglas de préstamo.",
						"No hay ninguna fila en parametros_institucion para el esquema en curso."));
	}

}
