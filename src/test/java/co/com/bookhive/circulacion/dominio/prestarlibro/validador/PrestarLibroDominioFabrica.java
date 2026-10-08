package co.com.bookhive.circulacion.dominio.prestarlibro.validador;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;

final class PrestarLibroDominioFabrica {

	private PrestarLibroDominioFabrica() {
		super();
	}

	static PrestarLibroDominio valido() {
		return new PrestarLibroDominio(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				LocalDate.now());
	}

	static PrestarLibroDominio sinEjemplar() {
		return new PrestarLibroDominio(
				UUID.randomUUID(), null, UUID.randomUUID(),
				UUID.randomUUID(), UUID.randomUUID(), LocalDate.now());
	}

	static PrestarLibroDominio sinUsuario() {
		return new PrestarLibroDominio(
				UUID.randomUUID(), UUID.randomUUID(), null,
				UUID.randomUUID(), UUID.randomUUID(), LocalDate.now());
	}

	static PrestarLibroDominio sinInstitucion() {
		return new PrestarLibroDominio(
				UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				null, UUID.randomUUID(), LocalDate.now());
	}

	static PrestarLibroDominio sinBibliotecario() {
		return new PrestarLibroDominio(
				UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), null, LocalDate.now());
	}

	static PrestarLibroDominio sinFecha() {
		return new PrestarLibroDominio(
				UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), UUID.randomUUID(), null);
	}

	static PrestarLibroDominio conFechaFutura() {
		return new PrestarLibroDominio(
				UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().plusDays(1));
	}

}
