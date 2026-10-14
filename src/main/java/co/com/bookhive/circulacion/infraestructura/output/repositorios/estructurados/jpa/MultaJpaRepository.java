package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.MultaEntidad;

public interface MultaJpaRepository extends JpaRepository<MultaEntidad, UUID> {

	boolean existsByUsuarioAndEstado(UUID usuario, String estado);

}
