package co.com.bookhive.circulacion.servicios.output.ports;

import java.util.UUID;

// Quién está atendiendo en este momento (el bibliotecario que inició sesión)
// y a qué institución pertenece. El negocio no sabe de dónde sale ese dato:
// el adaptador lo toma del usuario autenticado.
public interface UsuarioEnSesionPort {

	UUID obtenerIdentificador();

	UUID obtenerInstitucion();

}
