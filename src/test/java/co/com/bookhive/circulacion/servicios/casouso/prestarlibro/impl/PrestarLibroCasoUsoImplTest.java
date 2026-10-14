package co.com.bookhive.circulacion.servicios.casouso.prestarlibro.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.output.ports.EjemplarRepositorioPort;
import co.com.bookhive.circulacion.servicios.output.ports.MultaRepositorioPort;
import co.com.bookhive.circulacion.servicios.output.ports.ParametrosInstitucionPort;
import co.com.bookhive.circulacion.servicios.output.ports.PrestamoRepositorioPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.UUIDHelper;

// Pruebas del caso de uso sin base de datos: los puertos de salida se
// reemplazan por versiones de mentira que responden lo que cada prueba necesita.
class PrestarLibroCasoUsoImplTest {

	private static final int DIAS_DE_PRESTAMO = 15;
	private static final int MAXIMO_PRESTAMOS = 3;
	private static final LocalDate HOY = LocalDate.now();

	@Test
	@DisplayName("registra el préstamo con identificador nuevo y fecha de entrega, y lo guarda")
	void registraElPrestamo() {
		final PrestamosDePrueba prestamos = new PrestamosDePrueba(0);
		final PrestarLibroCasoUsoImpl casoUso = crearCasoUso(new EjemplaresDePrueba(true, true, true),
				new MultasDePrueba(false), prestamos);

		final PrestarLibroDominio resultado = casoUso.ejecutar(prestamoValido());

		assertThat(UUIDHelper.esValorDefecto(resultado.getIdentificador())).isFalse();
		assertThat(resultado.getFechaVencimiento()).isEqualTo(HOY.plusDays(DIAS_DE_PRESTAMO));
		assertThat(prestamos.vecesGuardado).isEqualTo(1);
		assertThat(prestamos.ultimoGuardado).isEqualTo(resultado);
	}

	@Test
	@DisplayName("rechaza el préstamo si el ejemplar no existe")
	void rechazaEjemplarInexistente() {
		final PrestamosDePrueba prestamos = new PrestamosDePrueba(0);
		final PrestarLibroCasoUsoImpl casoUso = crearCasoUso(new EjemplaresDePrueba(false, false, false),
				new MultasDePrueba(false), prestamos);

		assertThatThrownBy(() -> casoUso.ejecutar(prestamoValido())).isInstanceOf(BookHiveException.class);
		assertThat(prestamos.vecesGuardado).isEqualTo(0);
	}

	@Test
	@DisplayName("rechaza el préstamo si el ejemplar ya está prestado o reservado")
	void rechazaEjemplarNoDisponible() {
		final PrestamosDePrueba prestamos = new PrestamosDePrueba(0);
		final PrestarLibroCasoUsoImpl casoUso = crearCasoUso(new EjemplaresDePrueba(true, false, true),
				new MultasDePrueba(false), prestamos);

		assertThatThrownBy(() -> casoUso.ejecutar(prestamoValido())).isInstanceOf(BookHiveException.class);
		assertThat(prestamos.vecesGuardado).isEqualTo(0);
	}

	@Test
	@DisplayName("rechaza el préstamo si el ejemplar es solo de consulta en sala")
	void rechazaEjemplarSoloEnSala() {
		final PrestamosDePrueba prestamos = new PrestamosDePrueba(0);
		final PrestarLibroCasoUsoImpl casoUso = crearCasoUso(new EjemplaresDePrueba(true, true, false),
				new MultasDePrueba(false), prestamos);

		assertThatThrownBy(() -> casoUso.ejecutar(prestamoValido())).isInstanceOf(BookHiveException.class);
		assertThat(prestamos.vecesGuardado).isEqualTo(0);
	}

	@Test
	@DisplayName("rechaza el préstamo si el usuario tiene multas pendientes")
	void rechazaUsuarioConMultas() {
		final PrestamosDePrueba prestamos = new PrestamosDePrueba(0);
		final PrestarLibroCasoUsoImpl casoUso = crearCasoUso(new EjemplaresDePrueba(true, true, true),
				new MultasDePrueba(true), prestamos);

		assertThatThrownBy(() -> casoUso.ejecutar(prestamoValido())).isInstanceOf(BookHiveException.class);
		assertThat(prestamos.vecesGuardado).isEqualTo(0);
	}

	@Test
	@DisplayName("rechaza el préstamo si el usuario ya llegó al máximo de libros prestados")
	void rechazaUsuarioEnElMaximo() {
		final PrestamosDePrueba prestamos = new PrestamosDePrueba(MAXIMO_PRESTAMOS);
		final PrestarLibroCasoUsoImpl casoUso = crearCasoUso(new EjemplaresDePrueba(true, true, true),
				new MultasDePrueba(false), prestamos);

		assertThatThrownBy(() -> casoUso.ejecutar(prestamoValido())).isInstanceOf(BookHiveException.class);
		assertThat(prestamos.vecesGuardado).isEqualTo(0);
	}

	@Test
	@DisplayName("rechaza el préstamo si no llega el usuario")
	void rechazaPrestamoSinUsuario() {
		final PrestamosDePrueba prestamos = new PrestamosDePrueba(0);
		final PrestarLibroCasoUsoImpl casoUso = crearCasoUso(new EjemplaresDePrueba(true, true, true),
				new MultasDePrueba(false), prestamos);
		final PrestarLibroDominio sinUsuario = new PrestarLibroDominio(
				UUIDHelper.obtenerValorDefecto(), UUID.randomUUID(), UUIDHelper.obtenerValorDefecto(),
				UUID.randomUUID(), UUID.randomUUID(), HOY, null);

		assertThatThrownBy(() -> casoUso.ejecutar(sinUsuario)).isInstanceOf(BookHiveException.class);
		assertThat(prestamos.vecesGuardado).isEqualTo(0);
	}

	@Test
	@DisplayName("rechaza el préstamo si no llegan datos")
	void rechazaPrestamoNulo() {
		final PrestarLibroCasoUsoImpl casoUso = crearCasoUso(new EjemplaresDePrueba(true, true, true),
				new MultasDePrueba(false), new PrestamosDePrueba(0));

		assertThatThrownBy(() -> casoUso.ejecutar(null)).isInstanceOf(BookHiveException.class);
	}

	@Test
	@DisplayName("no deja crear el caso de uso si falta un puerto de salida")
	void exigeLosPuertosDeSalida() {
		final EjemplaresDePrueba ejemplares = new EjemplaresDePrueba(true, true, true);
		final MultasDePrueba multas = new MultasDePrueba(false);
		final PrestamosDePrueba prestamos = new PrestamosDePrueba(0);

		assertThatThrownBy(() -> new PrestarLibroCasoUsoImpl(null, ejemplares, multas, prestamos))
				.isInstanceOf(BookHiveException.class);
	}

	private static PrestarLibroCasoUsoImpl crearCasoUso(
			final EjemplarRepositorioPort ejemplares,
			final MultaRepositorioPort multas,
			final PrestamoRepositorioPort prestamos) {

		return new PrestarLibroCasoUsoImpl(new ParametrosDePrueba(), ejemplares, multas, prestamos);
	}

	private static PrestarLibroDominio prestamoValido() {
		return new PrestarLibroDominio(
				UUIDHelper.obtenerValorDefecto(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				HOY,
				null);
	}

	private static final class ParametrosDePrueba implements ParametrosInstitucionPort {

		@Override
		public int obtenerDiasDePrestamo() {
			return DIAS_DE_PRESTAMO;
		}

		@Override
		public int obtenerMaximoPrestamosSimultaneos() {
			return MAXIMO_PRESTAMOS;
		}

	}

	private static final class EjemplaresDePrueba implements EjemplarRepositorioPort {

		private final boolean existe;
		private final boolean disponible;
		private final boolean prestable;

		private EjemplaresDePrueba(final boolean existe, final boolean disponible, final boolean prestable) {
			this.existe = existe;
			this.disponible = disponible;
			this.prestable = prestable;
		}

		@Override
		public boolean existe(final UUID ejemplar) {
			return existe;
		}

		@Override
		public boolean estaDisponible(final UUID ejemplar) {
			return disponible;
		}

		@Override
		public boolean esPrestable(final UUID ejemplar) {
			return prestable;
		}

	}

	private static final class MultasDePrueba implements MultaRepositorioPort {

		private final boolean tienePendientes;

		private MultasDePrueba(final boolean tienePendientes) {
			this.tienePendientes = tienePendientes;
		}

		@Override
		public boolean tienePendientes(final UUID usuario) {
			return tienePendientes;
		}

	}

	private static final class PrestamosDePrueba implements PrestamoRepositorioPort {

		private final int activos;
		private int vecesGuardado;
		private PrestarLibroDominio ultimoGuardado;

		private PrestamosDePrueba(final int activos) {
			this.activos = activos;
		}

		@Override
		public int contarActivosPorUsuario(final UUID usuario) {
			return activos;
		}

		@Override
		public void guardar(final PrestarLibroDominio prestamo) {
			vecesGuardado++;
			ultimoGuardado = prestamo;
		}

	}

}
