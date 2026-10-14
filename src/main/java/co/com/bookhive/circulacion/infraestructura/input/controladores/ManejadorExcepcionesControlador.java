package co.com.bookhive.circulacion.infraestructura.input.controladores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.com.bookhive.circulacion.infraestructura.input.controladores.respuesta.RespuestaErrorDTO;
import co.com.bookhive.transversal.excepcion.BookHiveException;

// Cuando una regla del negocio rechaza la solicitud (por ejemplo, el usuario
// tiene multas), el cliente recibe 400 con el mensaje para el usuario, y el
// mensaje técnico queda solo en el registro del servidor.
@RestControllerAdvice
public class ManejadorExcepcionesControlador {

	private static final Logger REGISTRO = LoggerFactory.getLogger(ManejadorExcepcionesControlador.class);

	@ExceptionHandler(BookHiveException.class)
	public ResponseEntity<RespuestaErrorDTO> manejar(final BookHiveException excepcion) {
		REGISTRO.warn("Solicitud rechazada: {}", excepcion.getMensajeTecnico());
		return ResponseEntity.badRequest().body(new RespuestaErrorDTO(excepcion.getMensajeUsuario()));
	}

}
