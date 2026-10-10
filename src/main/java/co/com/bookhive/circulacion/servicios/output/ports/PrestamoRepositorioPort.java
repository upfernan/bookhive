package co.com.bookhive.circulacion.servicios.output.ports;

import java.util.UUID;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;

public interface PrestamoRepositorioPort {

	int contarActivosPorUsuario(UUID usuario);

	// El caso de uso entrega dominio; el adaptador lo convierte en entidad.
	void guardar(PrestarLibroDominio prestamo);

}
