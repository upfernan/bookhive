package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.UUIDHelper;

public final class ValidadorBibliotecario implements Validador {

	private static final Validador INSTANCIA = new ValidadorBibliotecario();

	private ValidadorBibliotecario() {
		super();
	}

	public static void ejecutarValidacion(final PrestarLibroDominio dominio) {
		INSTANCIA.validar(dominio);
	}

	@Override
	public void validar(final PrestarLibroDominio dominio) {

		if (UUIDHelper.esValorDefecto(dominio.getBibliotecario())) {
			throw BookHiveException.crear(
					"No fue posible establecer quién está registrando el préstamo.",
					"El bibliotecario llegó vacío en PrestarLibroDominio; debe resolverse desde el token.");
		}
	}

}
