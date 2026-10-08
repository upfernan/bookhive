package co.com.bookhive.transversal.helper;

public final class ObjectHelper {

	private ObjectHelper() {
		super();
	}

	public static <T> T obtenerValorDefecto(final T valor, final T valorDefecto) {
		return esNulo(valor) ? valorDefecto : valor;
	}

	public static boolean esNulo(final Object valor) {
		return valor == null;
	}

}
