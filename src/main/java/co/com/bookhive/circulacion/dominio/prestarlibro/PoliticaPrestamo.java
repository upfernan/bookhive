package co.com.bookhive.circulacion.dominio.prestarlibro;

import java.time.LocalDate;

import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.FechaHelper;

public final class PoliticaPrestamo {

	private PoliticaPrestamo() {
		super();
	}

	// El plazo entra por parámetro a propósito: lo define cada institución
	// (RN-10) y llega desde ParametrosInstitucionPort. Nunca quemar un número aquí.
	public static LocalDate calcularFechaVencimiento(
			final LocalDate fechaPrestamo,
			final int diasDePrestamo) {

		if (FechaHelper.esValorDefecto(fechaPrestamo)) {
			throw BookHiveException.crear(
					"No fue posible calcular la fecha de entrega del ejemplar.",
					"Se intentó calcular el vencimiento sin fecha de préstamo.");
		}

		if (diasDePrestamo <= 0) {
			throw BookHiveException.crear(
					"La institución no tiene configurado el plazo del préstamo.",
					"El plazo recibido fue " + diasDePrestamo + " días; debe ser mayor que cero.");
		}

		return fechaPrestamo.plusDays(diasDePrestamo);
	}

}
