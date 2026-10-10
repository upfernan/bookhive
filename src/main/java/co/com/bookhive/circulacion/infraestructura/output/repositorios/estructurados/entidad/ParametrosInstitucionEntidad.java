package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad;

import java.util.UUID;

import co.com.bookhive.transversal.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Representa la tabla "parametros_institucion": las reglas de préstamo que
// el administrador de cada institución ajusta desde su pantalla (ADR-017).
// Vive en el esquema de cada institución, así que tiene una sola fila.
// No es el catálogo de parámetros de la plataforma.
@Entity
@Table(name = "parametros_institucion")
public class ParametrosInstitucionEntidad {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID identificador;

	@Column(name = "dias_prestamo", nullable = false)
	private int diasPrestamo;

	@Column(name = "maximo_prestamos_simultaneos", nullable = false)
	private int maximoPrestamosSimultaneos;

	public ParametrosInstitucionEntidad() {
		super();
		setIdentificador(UUIDHelper.obtenerValorDefecto());
		setDiasPrestamo(0);
		setMaximoPrestamosSimultaneos(0);
	}

	public UUID getIdentificador() {
		return identificador;
	}

	public void setIdentificador(final UUID identificador) {
		this.identificador = UUIDHelper.obtenerValorDefecto(identificador);
	}

	public int getDiasPrestamo() {
		return diasPrestamo;
	}

	public void setDiasPrestamo(final int diasPrestamo) {
		this.diasPrestamo = diasPrestamo;
	}

	public int getMaximoPrestamosSimultaneos() {
		return maximoPrestamosSimultaneos;
	}

	public void setMaximoPrestamosSimultaneos(final int maximoPrestamosSimultaneos) {
		this.maximoPrestamosSimultaneos = maximoPrestamosSimultaneos;
	}

}
