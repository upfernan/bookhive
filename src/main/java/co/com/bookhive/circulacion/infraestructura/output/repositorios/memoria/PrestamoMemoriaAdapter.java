package co.com.bookhive.circulacion.infraestructura.output.repositorios.memoria;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.PrestamoEntidad;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.mapeador.PrestamoEntidadMapeador;
import co.com.bookhive.circulacion.servicios.output.ports.PrestamoRepositorioPort;

// TEMPORAL: adaptador de salida de préstamos sobre la base en memoria.
// Igual que hará el adaptador real: recibe dominio del caso de uso y lo
// convierte en entidad antes de guardarlo.
@Repository
public class PrestamoMemoriaAdapter implements PrestamoRepositorioPort {

	private final BaseDatosMemoria baseDatos;

	public PrestamoMemoriaAdapter(final BaseDatosMemoria baseDatos) {
		super();
		this.baseDatos = baseDatos;
	}

	@Override
	public int contarActivosPorUsuario(final UUID usuario) {
		return (int) baseDatos.listarPrestamos().stream()
				.filter(prestamo -> prestamo.getUsuario().equals(usuario)
						&& PrestamoEntidad.ESTADO_ACTIVO.equals(prestamo.getEstado()))
				.count();
	}

	@Override
	public void guardar(final PrestarLibroDominio prestamo) {
		baseDatos.guardarPrestamo(PrestamoEntidadMapeador.aEntidad(prestamo));
	}

}
