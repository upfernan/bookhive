package co.com.bookhive.circulacion.servicios.casouso.prestarlibro.validador;

import java.util.UUID;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.output.ports.EjemplarRepositorioPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;

public final class ValidadorEjemplarDisponible {

	private ValidadorEjemplarDisponible() {
		super();
	}

	public static void ejecutarValidacion(
			final PrestarLibroDominio dominio,
			final EjemplarRepositorioPort ejemplarRepositorio) {

		final UUID ejemplar = dominio.getEjemplar();

		if (!ejemplarRepositorio.existe(ejemplar)) {
			throw BookHiveException.crear(
					"El ejemplar indicado no existe en el catálogo.",
					"No se encontró el ejemplar " + ejemplar + " en la institución en curso.");
		}

		if (!ejemplarRepositorio.estaDisponible(ejemplar)) {
			throw BookHiveException.crear(
					"El ejemplar no está disponible: ya está prestado o reservado.",
					"El ejemplar " + ejemplar + " no se encuentra en estado disponible.");
		}
	}

}
