package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import java.time.LocalDate;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.FechaHelper;

public final class ValidadorFechaPrestamo implements Validador {

	private static final Validador INSTANCIA = new ValidadorFechaPrestamo();

	private ValidadorFechaPrestamo() {
		super();
	}

	public static void ejecutarValidacion(final PrestarLibroDominio dominio) {
		INSTANCIA.validar(dominio);
	}

	@Override
	public void validar(final PrestarLibroDominio dominio) {

		final LocalDate fechaPrestamo = FechaHelper.obtenerValorDefecto(dominio.getFechaPrestamo());

		if (FechaHelper.esValorDefecto(fechaPrestamo)) {
			throw BookHiveException.crear(
					"No fue posible establecer la fecha del préstamo.",
					"La fecha del préstamo llegó vacía en PrestarLibroDominio; la fija el servidor.");
		}

		if (fechaPrestamo.isAfter(FechaHelper.hoyEnUtc())) {
			throw BookHiveException.crear(
					"No se puede registrar un préstamo con fecha futura.",
					"La fecha del préstamo es posterior a la fecha actual.");
		}
	}

}
