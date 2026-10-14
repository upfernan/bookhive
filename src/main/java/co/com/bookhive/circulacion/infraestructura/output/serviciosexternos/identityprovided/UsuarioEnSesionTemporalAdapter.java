package co.com.bookhive.circulacion.infraestructura.output.serviciosexternos.identityprovided;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.com.bookhive.circulacion.servicios.output.ports.UsuarioEnSesionPort;

// TEMPORAL: mientras no hay inicio de sesión, el bibliotecario y la
// institución salen de application.properties. Cuando se conecte el
// proveedor de identidad (Identity Platform, ADR-026) se leerán del token
// del usuario autenticado.
@Component
public class UsuarioEnSesionTemporalAdapter implements UsuarioEnSesionPort {

	private final UUID bibliotecario;
	private final UUID institucion;

	public UsuarioEnSesionTemporalAdapter(
			@Value("${bookhive.temporal.bibliotecario:cccccccc-cccc-cccc-cccc-cccccccccccc}") final UUID bibliotecario,
			@Value("${bookhive.temporal.institucion:dddddddd-dddd-dddd-dddd-dddddddddddd}") final UUID institucion) {

		super();
		this.bibliotecario = bibliotecario;
		this.institucion = institucion;
	}

	@Override
	public UUID obtenerIdentificador() {
		return bibliotecario;
	}

	@Override
	public UUID obtenerInstitucion() {
		return institucion;
	}

}
