package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.EjemplarEntidad;

// Repositorio de Spring Data: solo habla SQL. Quien traduce al lenguaje del
// negocio es el adaptador.
public interface EjemplarJpaRepository extends JpaRepository<EjemplarEntidad, UUID> {

	boolean existsByIdentificadorAndEstado(UUID identificador, String estado);

	boolean existsByIdentificadorAndPrestable(UUID identificador, boolean prestable);

}
