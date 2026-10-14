package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.adaptador;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.PrestamoEntidad;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.jpa.PrestamoJpaRepository;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.mapeador.PrestamoEntidadMapeador;
import co.com.bookhive.circulacion.servicios.output.ports.PrestamoRepositorioPort;

// Aquí es donde el dominio se vuelve entidad. El caso de uso entrega dominio y
// no sabe que existe una tabla; este adaptador traduce y guarda.
@Repository
public class PrestamoRepositorioAdaptador implements PrestamoRepositorioPort {

	private final PrestamoJpaRepository repositorio;

	public PrestamoRepositorioAdaptador(final PrestamoJpaRepository repositorio) {
		super();
		this.repositorio = repositorio;
	}

	@Override
	public int contarActivosPorUsuario(final UUID usuario) {
		return repositorio.countByUsuarioAndEstado(usuario, PrestamoEntidad.ESTADO_ACTIVO);
	}

	@Override
	public void guardar(final PrestarLibroDominio prestamo) {
		final PrestamoEntidad entidad = PrestamoEntidadMapeador.aEntidad(prestamo);
		repositorio.save(entidad);
	}

}
