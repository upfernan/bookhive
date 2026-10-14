package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.adaptador;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.EjemplarEntidad;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.jpa.EjemplarJpaRepository;
import co.com.bookhive.circulacion.servicios.output.ports.EjemplarRepositorioPort;

// Implementa el puerto que declaró el negocio. El negocio pregunta "¿está
// disponible?" y no sabe que la respuesta sale de una tabla.
@Repository
public class EjemplarRepositorioAdaptador implements EjemplarRepositorioPort {

	private final EjemplarJpaRepository repositorio;

	public EjemplarRepositorioAdaptador(final EjemplarJpaRepository repositorio) {
		super();
		this.repositorio = repositorio;
	}

	@Override
	public boolean existe(final UUID ejemplar) {
		return repositorio.existsById(ejemplar);
	}

	@Override
	public boolean estaDisponible(final UUID ejemplar) {
		return repositorio.existsByIdentificadorAndEstado(
				ejemplar, EjemplarEntidad.ESTADO_DISPONIBLE);
	}

	@Override
	public boolean esPrestable(final UUID ejemplar) {
		return repositorio.existsByIdentificadorAndPrestable(ejemplar, true);
	}

}
