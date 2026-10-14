package co.com.bookhive.circulacion.servicios.input.adapters.mapeador;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.input.dto.request.PrestarLibroRequestDTO;
import co.com.bookhive.circulacion.servicios.input.dto.response.PrestarLibroResponseDTO;
import co.com.bookhive.transversal.helper.FechaHelper;
import co.com.bookhive.transversal.helper.ObjectHelper;
import co.com.bookhive.transversal.helper.UUIDHelper;

// Convierte el DTO que llega del controlador en el dominio del préstamo, y el
// dominio que devuelve el caso de uso en el DTO de respuesta.
// Mapeo a mano, sin MapStruct, como pidió el profe.
public final class PrestarLibroMapeador {

	private PrestarLibroMapeador() {
		super();
	}

	// El DTO solo trae ejemplar y usuario. El resto lo completa el sistema:
	// quién atiende, su institución y la fecha de hoy. El identificador y la
	// fecha de entrega los pone después el caso de uso.
	public static PrestarLibroDominio aDominio(
			final PrestarLibroRequestDTO peticion,
			final UUID bibliotecario,
			final UUID institucion,
			final LocalDate fechaPrestamo) {

		final PrestarLibroRequestDTO datos = ObjectHelper.obtenerValorDefecto(peticion, new PrestarLibroRequestDTO());

		return new PrestarLibroDominio(
				UUIDHelper.obtenerValorDefecto(),
				datos.getEjemplar(),
				datos.getUsuario(),
				institucion,
				bibliotecario,
				fechaPrestamo,
				FechaHelper.obtenerValorDefecto());
	}

	// La respuesta solo lleva lo que se muestra: el número del préstamo y la
	// fecha en que hay que devolver el libro.
	public static PrestarLibroResponseDTO aRespuesta(final PrestarLibroDominio prestamo) {

		if (ObjectHelper.esNulo(prestamo)) {
			return new PrestarLibroResponseDTO();
		}

		return new PrestarLibroResponseDTO(prestamo.getIdentificador(), prestamo.getFechaVencimiento());
	}

}
