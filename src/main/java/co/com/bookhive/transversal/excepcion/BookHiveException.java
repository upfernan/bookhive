package co.com.bookhive.transversal.excepcion;

import co.com.bookhive.transversal.helper.ObjectHelper;

public class BookHiveException extends RuntimeException {

	private static final long serialVersionUID = -693023700649543159L;

	private final transient Throwable causaRaiz;
	private final String mensajeUsuario;
	private final String mensajeTecnico;

	private BookHiveException(
			final Throwable causaRaiz,
			final String mensajeUsuario,
			final String mensajeTecnico) {

		super(mensajeTecnico);
		this.causaRaiz = ObjectHelper.obtenerValorDefecto(causaRaiz, new Exception());
		this.mensajeUsuario = ObjectHelper.obtenerValorDefecto(mensajeUsuario, "");
		this.mensajeTecnico = ObjectHelper.obtenerValorDefecto(mensajeTecnico, "");
	}

	public static BookHiveException crear(final String mensajeUsuario) {
		return new BookHiveException(new Exception(), mensajeUsuario, mensajeUsuario);
	}

	public static BookHiveException crear(final String mensajeUsuario, final String mensajeTecnico) {
		return new BookHiveException(new Exception(), mensajeUsuario, mensajeTecnico);
	}

	public static BookHiveException crear(
			final Throwable causaRaiz,
			final String mensajeUsuario,
			final String mensajeTecnico) {

		return new BookHiveException(causaRaiz, mensajeUsuario, mensajeTecnico);
	}

	public Throwable getCausaRaiz() {
		return causaRaiz;
	}

	public String getMensajeUsuario() {
		return mensajeUsuario;
	}

	public String getMensajeTecnico() {
		return mensajeTecnico;
	}

}
