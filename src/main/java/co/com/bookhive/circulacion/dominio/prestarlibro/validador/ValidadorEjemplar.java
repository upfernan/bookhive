package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.UUIDHelper;

public final class ValidadorEjemplar implements Validador {

	private static final Validador INSTANCIA = new ValidadorEjemplar();

	private ValidadorEjemplar() {
		super();
	}

	public static void ejecutarValidacion(final PrestarLibroDominio dominio) {
		INSTANCIA.validar(dominio);
	}

	@Override
	public void validar(final PrestarLibroDominio dominio) {

		if (UUIDHelper.esValorDefecto(dominio.getEjemplar())) {
			throw BookHiveException.crear(
					"Debe indicar cuál ejemplar se va a prestar.",
					"El identificador del ejemplar llegó vacío en PrestarLibroDominio.");
		}
	}

}
