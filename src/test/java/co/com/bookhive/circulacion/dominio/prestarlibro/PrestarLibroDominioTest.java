package co.com.bookhive.circulacion.dominio.prestarlibro;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.transversal.helper.FechaHelper;
import co.com.bookhive.transversal.helper.UUIDHelper;

class PrestarLibroDominioTest {

	@Test
	@DisplayName("cuando todo llega nulo, ningún campo queda nulo")
	void cuandoTodoLlegaNuloNingunCampoQuedaNulo() {

		final PrestarLibroDominio dominio =
				new PrestarLibroDominio(null, null, null, null, null, null, null);

		assertThat(dominio.getIdentificador()).isEqualTo(UUIDHelper.obtenerValorDefecto());
		assertThat(dominio.getEjemplar()).isEqualTo(UUIDHelper.obtenerValorDefecto());
		assertThat(dominio.getUsuario()).isEqualTo(UUIDHelper.obtenerValorDefecto());
		assertThat(dominio.getInstitucion()).isEqualTo(UUIDHelper.obtenerValorDefecto());
		assertThat(dominio.getBibliotecario()).isEqualTo(UUIDHelper.obtenerValorDefecto());
		assertThat(dominio.getFechaPrestamo()).isEqualTo(FechaHelper.obtenerValorDefecto());
		assertThat(dominio.getFechaVencimiento()).isEqualTo(FechaHelper.obtenerValorDefecto());
	}

	@Test
	@DisplayName("conserva los valores que recibe")
	void conservaLosValoresQueRecibe() {

		final UUID identificador = UUID.randomUUID();
		final UUID ejemplar = UUID.randomUUID();
		final UUID usuario = UUID.randomUUID();
		final UUID institucion = UUID.randomUUID();
		final UUID bibliotecario = UUID.randomUUID();
		final LocalDate fechaPrestamo = FechaHelper.hoyEnUtc();
		final LocalDate fechaVencimiento = fechaPrestamo.plusDays(15);

		final PrestarLibroDominio dominio = new PrestarLibroDominio(
				identificador, ejemplar, usuario, institucion,
				bibliotecario, fechaPrestamo, fechaVencimiento);

		assertThat(dominio.getIdentificador()).isEqualTo(identificador);
		assertThat(dominio.getEjemplar()).isEqualTo(ejemplar);
		assertThat(dominio.getUsuario()).isEqualTo(usuario);
		assertThat(dominio.getInstitucion()).isEqualTo(institucion);
		assertThat(dominio.getBibliotecario()).isEqualTo(bibliotecario);
		assertThat(dominio.getFechaPrestamo()).isEqualTo(fechaPrestamo);
		assertThat(dominio.getFechaVencimiento()).isEqualTo(fechaVencimiento);
	}

	@Test
	@DisplayName("conFechaVencimiento devuelve una copia nueva sin tocar la original")
	void conFechaVencimientoDevuelveCopiaNueva() {

		final LocalDate hoy = FechaHelper.hoyEnUtc();

		final PrestarLibroDominio original = new PrestarLibroDominio(
				UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				UUID.randomUUID(), UUID.randomUUID(), hoy, null);

		final PrestarLibroDominio completo = original.conFechaVencimiento(hoy.plusDays(15));

		assertThat(original.getFechaVencimiento()).isEqualTo(FechaHelper.obtenerValorDefecto());
		assertThat(completo.getFechaVencimiento()).isEqualTo(hoy.plusDays(15));
		assertThat(completo.getEjemplar()).isEqualTo(original.getEjemplar());
	}

}
