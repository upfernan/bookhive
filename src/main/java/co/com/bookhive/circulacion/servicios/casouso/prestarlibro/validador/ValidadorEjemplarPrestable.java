package co.com.bookhive.circulacion.servicios.casouso.prestarlibro.validador;

import java.util.UUID;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.output.ports.EjemplarRepositorioPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;

public final class ValidadorEjemplarPrestable {

	private ValidadorEjemplarPrestable() {
		super();
	}

	public static void ejecutarValidacion(
			final PrestarLibroDominio dominio,
			final EjemplarRepositorioPort ejemplarRepositorio) {

		final UUID ejemplar = dominio.getEjemplar();

		if (!ejemplarRepositorio.esPrestable(ejemplar)) {
			throw BookHiveException.crear(
					"Este ejemplar solo puede consultarse en la sala; no se presta a domicilio.",
					"El ejemplar " + ejemplar + " está marcado como de consulta en sala.");
		}
	}

}
