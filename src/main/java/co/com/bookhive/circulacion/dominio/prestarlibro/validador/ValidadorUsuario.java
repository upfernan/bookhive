package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.UUIDHelper;

public final class ValidadorUsuario implements Validador {

	private static final Validador INSTANCIA = new ValidadorUsuario();

	private ValidadorUsuario() {
		super();
	}

	public static void ejecutarValidacion(final PrestarLibroDominio dominio) {
		INSTANCIA.validar(dominio);
	}

	@Override
	public void validar(final PrestarLibroDominio dominio) {

		if (UUIDHelper.esValorDefecto(dominio.getUsuario())) {
			throw BookHiveException.crear(
					"Debe indicar a cuál usuario se le presta el ejemplar.",
					"El identificador del usuario llegó vacío en PrestarLibroDominio.");
		}
	}

}
