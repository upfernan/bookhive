package co.com.bookhive.circulacion.infraestructura.input.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.bookhive.circulacion.servicios.input.dto.request.PrestarLibroRequestDTO;
import co.com.bookhive.circulacion.servicios.input.dto.response.PrestarLibroResponseDTO;
import co.com.bookhive.circulacion.servicios.input.ports.PrestarLibroInputPort;

@RestController
@RequestMapping("/api/v1/prestamos")
public class PrestamoControlador {

	private final PrestarLibroInputPort prestarLibroInputPort;

	public PrestamoControlador(final PrestarLibroInputPort prestarLibroInputPort) {
		super();
		this.prestarLibroInputPort = prestarLibroInputPort;
	}

	@PostMapping
	public ResponseEntity<PrestarLibroResponseDTO> prestar(
			@RequestBody final PrestarLibroRequestDTO peticion) {

		final PrestarLibroResponseDTO respuesta = prestarLibroInputPort.ejecutar(peticion);

		return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
	}

}
