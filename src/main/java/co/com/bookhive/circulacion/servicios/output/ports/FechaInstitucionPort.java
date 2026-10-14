package co.com.bookhive.circulacion.servicios.output.ports;

import java.time.LocalDate;

// La fecha de hoy según la zona horaria de la institución en curso (ADR-016).
// No se usa la fecha del servidor: el servidor puede estar en otro país.
public interface FechaInstitucionPort {

	LocalDate obtenerFechaActual();

}
