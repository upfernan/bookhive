package co.com.bookhive.circulacion.dominio.prestarlibro;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.transversal.helper.FechaHelper;
import co.com.bookhive.transversal.helper.UUIDHelper;

public final class PrestarLibroDominio {

	private UUID identificador;
	private UUID ejemplar;
	private UUID usuario;
	private UUID institucion;
	private UUID bibliotecario;
	private LocalDate fechaPrestamo;

	public PrestarLibroDominio(
			final UUID identificador,
			final UUID ejemplar,
			final UUID usuario,
			final UUID institucion,
			final UUID bibliotecario,
			final LocalDate fechaPrestamo) {

		super();
		setIdentificador(identificador);
		setEjemplar(ejemplar);
		setUsuario(usuario);
		setInstitucion(institucion);
		setBibliotecario(bibliotecario);
		setFechaPrestamo(fechaPrestamo);
	}

	public UUID getIdentificador() {
		return identificador;
	}

	public UUID getEjemplar() {
		return ejemplar;
	}

	public UUID getUsuario() {
		return usuario;
	}

	public UUID getInstitucion() {
		return institucion;
	}

	public UUID getBibliotecario() {
		return bibliotecario;
	}

	public LocalDate getFechaPrestamo() {
		return fechaPrestamo;
	}

	private void setIdentificador(final UUID identificador) {
		this.identificador = UUIDHelper.obtenerValorDefecto(identificador);
	}

	private void setEjemplar(final UUID ejemplar) {
		this.ejemplar = UUIDHelper.obtenerValorDefecto(ejemplar);
	}

	private void setUsuario(final UUID usuario) {
		this.usuario = UUIDHelper.obtenerValorDefecto(usuario);
	}

	private void setInstitucion(final UUID institucion) {
		this.institucion = UUIDHelper.obtenerValorDefecto(institucion);
	}

	private void setBibliotecario(final UUID bibliotecario) {
		this.bibliotecario = UUIDHelper.obtenerValorDefecto(bibliotecario);
	}

	private void setFechaPrestamo(final LocalDate fechaPrestamo) {
		this.fechaPrestamo = FechaHelper.obtenerValorDefecto(fechaPrestamo);
	}

}
