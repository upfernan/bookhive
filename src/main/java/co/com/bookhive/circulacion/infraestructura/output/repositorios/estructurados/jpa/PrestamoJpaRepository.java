package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.PrestamoEntidad;

public interface PrestamoJpaRepository extends JpaRepository<PrestamoEntidad, UUID> {

	int countByUsuarioAndEstado(UUID usuario, String estado);

}
