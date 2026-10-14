package co.com.bookhive.circulacion.infraestructura.output.repositorios.memoria;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.MultaEntidad;
import co.com.bookhive.circulacion.servicios.output.ports.MultaRepositorioPort;

// TEMPORAL: adaptador de salida de multas sobre la base en memoria.
@Repository
public class MultaMemoriaAdapter implements MultaRepositorioPort {

	private final BaseDatosMemoria baseDatos;

	public MultaMemoriaAdapter(final BaseDatosMemoria baseDatos) {
		super();
		this.baseDatos = baseDatos;
	}

	@Override
	public boolean tienePendientes(final UUID usuario) {
		return baseDatos.listarMultas().stream()
				.anyMatch(multa -> multa.getUsuario().equals(usuario)
						&& MultaEntidad.ESTADO_PENDIENTE.equals(multa.getEstado()));
	}

}
