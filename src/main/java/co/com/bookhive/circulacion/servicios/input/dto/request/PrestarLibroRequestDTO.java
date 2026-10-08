package co.com.bookhive.circulacion.servicios.input.dto.request;

import java.util.UUID;

import co.com.bookhive.transversal.helper.UUIDHelper;

public final class PrestarLibroRequestDTO {

	private UUID ejemplar;
	private UUID usuario;

	public PrestarLibroRequestDTO() {
		super();
		setEjemplar(UUIDHelper.obtenerValorDefecto());
		setUsuario(UUIDHelper.obtenerValorDefecto());
	}

	public PrestarLibroRequestDTO(final UUID ejemplar, final UUID usuario) {
		super();
		setEjemplar(ejemplar);
		setUsuario(usuario);
	}

	public UUID getEjemplar() {
		return ejemplar;
	}

	public UUID getUsuario() {
		return usuario;
	}

	private void setEjemplar(final UUID ejemplar) {
		this.ejemplar = UUIDHelper.obtenerValorDefecto(ejemplar);
	}

	private void setUsuario(final UUID usuario) {
		this.usuario = UUIDHelper.obtenerValorDefecto(usuario);
	}

}
