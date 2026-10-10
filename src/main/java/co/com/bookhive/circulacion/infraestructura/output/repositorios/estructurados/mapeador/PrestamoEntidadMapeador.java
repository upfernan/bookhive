package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.mapeador;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad.PrestamoEntidad;

// Convierte el dominio de "prestar libro" en la entidad que se guarda.
// Mapeo a mano, sin MapStruct, como pidió el profe. La institución no se
// copia porque la define el esquema al que se conecta (ADR-011).
public final class PrestamoEntidadMapeador {

	private PrestamoEntidadMapeador() {
		super();
	}

	public static PrestamoEntidad aEntidad(final PrestarLibroDominio dominio) {
		final PrestamoEntidad entidad = new PrestamoEntidad();
		entidad.setIdentificador(dominio.getIdentificador());
		entidad.setEjemplar(dominio.getEjemplar());
		entidad.setUsuario(dominio.getUsuario());
		entidad.setBibliotecario(dominio.getBibliotecario());
		entidad.setFechaPrestamo(dominio.getFechaPrestamo());
		entidad.setFechaVencimiento(dominio.getFechaVencimiento());
		entidad.setEstado(PrestamoEntidad.ESTADO_ACTIVO);
		return entidad;
	}

}
