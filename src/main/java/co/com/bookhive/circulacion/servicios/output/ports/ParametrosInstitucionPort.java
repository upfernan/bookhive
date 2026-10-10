package co.com.bookhive.circulacion.servicios.output.ports;

// NOTA SUEPR IMPROTANTE: estos valores NO van al parameter catalog.
// Son configuración POR INSTITUCIÓN, editable por el administrador institucional RECORDAR QUE ES DIEFERENTE A LO QEU SE TIENE QUE TRAER DE ALLA 
public interface ParametrosInstitucionPort {

	int obtenerDiasDePrestamo();

	int obtenerMaximoPrestamosSimultaneos();

}
