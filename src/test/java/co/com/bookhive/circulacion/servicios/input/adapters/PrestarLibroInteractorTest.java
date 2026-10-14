package co.com.bookhive.circulacion.servicios.input.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.casouso.prestarlibro.PrestarLibroCasoUso;
import co.com.bookhive.circulacion.servicios.input.dto.request.PrestarLibroRequestDTO;
import co.com.bookhive.circulacion.servicios.input.dto.response.PrestarLibroResponseDTO;
import co.com.bookhive.circulacion.servicios.output.ports.FechaInstitucionPort;
import co.com.bookhive.circulacion.servicios.output.ports.UsuarioEnSesionPort;
import co.com.bookhive.transversal.excepcion.BookHiveException;
import co.com.bookhive.transversal.helper.UUIDHelper;

// Pruebas del adaptador de entrada sin Spring ni base de datos: el caso de uso
// y los puertos se reemplazan por versiones de prueba.
class PrestarLibroInteractorTest {

	private static final UUID BIBLIOTECARIO = UUID.randomUUID();
	private static final UUID INSTITUCION = UUID.randomUUID();
	private static final LocalDate HOY = LocalDate.of(2026, 10, 14);

	@Test
	@DisplayName("arma el dominio con los datos del DTO, de la sesión y la fecha de la institución")
	void armaElDominio() {
		final CasoUsoDePrueba casoUso = new CasoUsoDePrueba();
		final PrestarLibroInteractor interactor = crearInteractor(casoUso);
		final UUID ejemplar = UUID.randomUUID();
		final UUID usuario = UUID.randomUUID();

		interactor.ejecutar(new PrestarLibroRequestDTO(ejemplar, usuario));

		final PrestarLibroDominio recibido = casoUso.recibido;
		assertThat(recibido.getEjemplar()).isEqualTo(ejemplar);
		assertThat(recibido.getUsuario()).isEqualTo(usuario);
		assertThat(recibido.getBibliotecario()).isEqualTo(BIBLIOTECARIO);
		assertThat(recibido.getInstitucion()).isEqualTo(INSTITUCION);
		assertThat(recibido.getFechaPrestamo()).isEqualTo(HOY);
	}

	@Test
	@DisplayName("devuelve el número del préstamo y la fecha de entrega")
	void devuelveLaRespuesta() {
		final CasoUsoDePrueba casoUso = new CasoUsoDePrueba();
		final PrestarLibroInteractor interactor = crearInteractor(casoUso);

		final PrestarLibroResponseDTO respuesta = interactor.ejecutar(
				new PrestarLibroRequestDTO(UUID.randomUUID(), UUID.randomUUID()));

		assertThat(respuesta.getPrestamo()).isEqualTo(casoUso.devuelto.getIdentificador());
		assertThat(respuesta.getFechaVencimiento()).isEqualTo(casoUso.devuelto.getFechaVencimiento());
	}

	@Test
	@DisplayName("si la petición llega vacía, el caso de uso recibe valores por defecto y no nulos")
	void peticionVacia() {
		final CasoUsoDePrueba casoUso = new CasoUsoDePrueba();
		final PrestarLibroInteractor interactor = crearInteractor(casoUso);

		interactor.ejecutar(null);

		assertThat(UUIDHelper.esValorDefecto(casoUso.recibido.getEjemplar())).isTrue();
		assertThat(UUIDHelper.esValorDefecto(casoUso.recibido.getUsuario())).isTrue();
	}

	@Test
	@DisplayName("no deja crear el adaptador si falta el caso de uso")
	void exigeElCasoDeUso() {
		final UsuarioEnSesionDePrueba sesion = new UsuarioEnSesionDePrueba();
		final FechaInstitucionPort fecha = () -> HOY;

		assertThatThrownBy(() -> new PrestarLibroInteractor(null, sesion, fecha))
				.isInstanceOf(BookHiveException.class);
	}

	private static PrestarLibroInteractor crearInteractor(final PrestarLibroCasoUso casoUso) {
		return new PrestarLibroInteractor(casoUso, new UsuarioEnSesionDePrueba(), () -> HOY);
	}

	private static final class CasoUsoDePrueba implements PrestarLibroCasoUso {

		private PrestarLibroDominio recibido;
		private PrestarLibroDominio devuelto;

		@Override
		public PrestarLibroDominio ejecutar(final PrestarLibroDominio dominio) {
			recibido = dominio;
			devuelto = new PrestarLibroDominio(
					UUID.randomUUID(),
					dominio.getEjemplar(),
					dominio.getUsuario(),
					dominio.getInstitucion(),
					dominio.getBibliotecario(),
					dominio.getFechaPrestamo(),
					dominio.getFechaPrestamo().plusDays(15));
			return devuelto;
		}

	}

	private static final class UsuarioEnSesionDePrueba implements UsuarioEnSesionPort {

		@Override
		public UUID obtenerIdentificador() {
			return BIBLIOTECARIO;
		}

		@Override
		public UUID obtenerInstitucion() {
			return INSTITUCION;
		}

	}

}
