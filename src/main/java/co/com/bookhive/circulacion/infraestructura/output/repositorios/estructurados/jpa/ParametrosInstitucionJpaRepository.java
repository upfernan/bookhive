package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.ParametrosInstitucionEntidad;

// La tabla tiene UNA sola fila por institución: el esquema al que se conecta
// ya define de qué universidad son estos parámetros (ADR-011).
public interface ParametrosInstitucionJpaRepository
		extends JpaRepository<ParametrosInstitucionEntidad, UUID> {

}
