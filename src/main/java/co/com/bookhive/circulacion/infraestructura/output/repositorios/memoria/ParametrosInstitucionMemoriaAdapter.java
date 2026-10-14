package co.com.bookhive.circulacion.infraestructura.output.repositorios.memoria;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.ParametrosInstitucionEntidad;
import co.com.bookhive.circulacion.servicios.output.ports.ParametrosInstitucionPort;

// TEMPORAL: las reglas de la institución salen de application.properties.
// Cuando exista la base de datos saldrán de la tabla parametros_institucion
// (ADR-017), que es lo que representa ParametrosInstitucionEntidad.
@Primary
@Profile("memoria")
@Repository
public class ParametrosInstitucionMemoriaAdapter implements ParametrosInstitucionPort {

	private final ParametrosInstitucionEntidad parametros = new ParametrosInstitucionEntidad();

	public ParametrosInstitucionMemoriaAdapter(
			@Value("${bookhive.temporal.dias-prestamo:15}") final int diasPrestamo,
			@Value("${bookhive.temporal.maximo-prestamos:3}") final int maximoPrestamos) {

		super();
		parametros.setDiasPrestamo(diasPrestamo);
		parametros.setMaximoPrestamosSimultaneos(maximoPrestamos);
	}

	@Override
	public int obtenerDiasDePrestamo() {
		return parametros.getDiasPrestamo();
	}

	@Override
	public int obtenerMaximoPrestamosSimultaneos() {
		return parametros.getMaximoPrestamosSimultaneos();
	}

}
