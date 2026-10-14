package co.com.bookhive.circulacion.infraestructura.output.repositorios.memoria;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulacion.servicios.output.ports.FechaInstitucionPort;

// TEMPORAL: la zona horaria de la institución sale de application.properties.
// Cuando exista la base de datos saldrá de la configuración de cada
// institución (ADR-016). Nunca se usa la zona del servidor.
@Component
public class FechaInstitucionMemoriaAdapter implements FechaInstitucionPort {

	private final ZoneId zonaHoraria;

	public FechaInstitucionMemoriaAdapter(
			@Value("${bookhive.temporal.zona-horaria:America/Bogota}") final String zonaHoraria) {

		super();
		this.zonaHoraria = ZoneId.of(zonaHoraria);
	}

	@Override
	public LocalDate obtenerFechaActual() {
		return LocalDate.now(zonaHoraria);
	}

}
