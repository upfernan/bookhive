package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.transversal.helper.FechaHelper;

final class PrestarLibroDominioFabrica {

	private static final int DIAS = 15;

	private PrestarLibroDominioFabrica() {
		super();
	}

	private static PrestarLibroDominio construir(
			final UUID ejemplar,
			final UUID usuario,
			final UUID institucion,
			final UUID bibliotecario,
			final LocalDate fechaPrestamo) {

		return new PrestarLibroDominio(
				UUID.randomUUID(),
				ejemplar,
				usuario,
				institucion,
				bibliotecario,
				fechaPrestamo,
				fechaPrestamo == null ? null : fechaPrestamo.plusDays(DIAS));
	}

	static PrestarLibroDominio valido() {
		return construir(UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), UUID.randomUUID(), FechaHelper.hoyEnUtc());
	}

	static PrestarLibroDominio sinEjemplar() {
		return construir(null, UUID.randomUUID(),
				UUID.randomUUID(), UUID.randomUUID(), FechaHelper.hoyEnUtc());
	}

	static PrestarLibroDominio sinUsuario() {
		return construir(UUID.randomUUID(), null,
				UUID.randomUUID(), UUID.randomUUID(), FechaHelper.hoyEnUtc());
	}

	static PrestarLibroDominio sinInstitucion() {
		return construir(UUID.randomUUID(), UUID.randomUUID(),
				null, UUID.randomUUID(), FechaHelper.hoyEnUtc());
	}

	static PrestarLibroDominio sinBibliotecario() {
		return construir(UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), null, FechaHelper.hoyEnUtc());
	}

	static PrestarLibroDominio sinFecha() {
		return construir(UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), UUID.randomUUID(), null);
	}

	static PrestarLibroDominio conFechaFutura() {
		return construir(UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), UUID.randomUUID(), FechaHelper.hoyEnUtc().plusDays(1));
	}

}
