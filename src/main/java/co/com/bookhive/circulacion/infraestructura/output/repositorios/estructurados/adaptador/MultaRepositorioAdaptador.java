package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.adaptador;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.MultaEntidad;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.jpa.MultaJpaRepository;
import co.com.bookhive.circulacion.servicios.output.ports.MultaRepositorioPort;

@Repository
public class MultaRepositorioAdaptador implements MultaRepositorioPort {

	private final MultaJpaRepository repositorio;

	public MultaRepositorioAdaptador(final MultaJpaRepository repositorio) {
		super();
		this.repositorio = repositorio;
	}

	@Override
	public boolean tienePendientes(final UUID usuario) {
		return repositorio.existsByUsuarioAndEstado(usuario, MultaEntidad.ESTADO_PENDIENTE);
	}

}
