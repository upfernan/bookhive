package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.transversal.helper.FechaHelper;
import co.com.bookhive.transversal.helper.ObjectHelper;
import co.com.bookhive.transversal.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Representa la tabla "prestamo". No lleva columna de institución porque cada
// institución tiene su propio esquema en la base de datos (ADR-011).
@Entity
@Table(name = "prestamo")
public class PrestamoEntidad {

	public static final String ESTADO_ACTIVO = "ACTIVO";
	public static final String ESTADO_VENCIDO = "VENCIDO";
	public static final String ESTADO_DEVUELTO = "DEVUELTO";

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID identificador;

	@Column(name = "ejemplar_id", nullable = false)
	private UUID ejemplar;

	@Column(name = "usuario_id", nullable = false)
	private UUID usuario;

	@Column(name = "bibliotecario_id", nullable = false)
	private UUID bibliotecario;

	@Column(name = "fecha_prestamo", nullable = false)
	private LocalDate fechaPrestamo;

	@Column(name = "fecha_vencimiento", nullable = false)
	private LocalDate fechaVencimiento;

	@Column(name = "estado", nullable = false, length = 20)
	private String estado;

	public PrestamoEntidad() {
		super();
		setIdentificador(UUIDHelper.obtenerValorDefecto());
		setEjemplar(UUIDHelper.obtenerValorDefecto());
		setUsuario(UUIDHelper.obtenerValorDefecto());
		setBibliotecario(UUIDHelper.obtenerValorDefecto());
		setFechaPrestamo(FechaHelper.obtenerValorDefecto());
		setFechaVencimiento(FechaHelper.obtenerValorDefecto());
		setEstado(ESTADO_ACTIVO);
	}

	public UUID getIdentificador() {
		return identificador;
	}

	public void setIdentificador(final UUID identificador) {
		this.identificador = UUIDHelper.obtenerValorDefecto(identificador);
	}

	public UUID getEjemplar() {
		return ejemplar;
	}

	public void setEjemplar(final UUID ejemplar) {
		this.ejemplar = UUIDHelper.obtenerValorDefecto(ejemplar);
	}

	public UUID getUsuario() {
		return usuario;
	}

	public void setUsuario(final UUID usuario) {
		this.usuario = UUIDHelper.obtenerValorDefecto(usuario);
	}

	public UUID getBibliotecario() {
		return bibliotecario;
	}

	public void setBibliotecario(final UUID bibliotecario) {
		this.bibliotecario = UUIDHelper.obtenerValorDefecto(bibliotecario);
	}

	public LocalDate getFechaPrestamo() {
		return fechaPrestamo;
	}

	public void setFechaPrestamo(final LocalDate fechaPrestamo) {
		this.fechaPrestamo = FechaHelper.obtenerValorDefecto(fechaPrestamo);
	}

	public LocalDate getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(final LocalDate fechaVencimiento) {
		this.fechaVencimiento = FechaHelper.obtenerValorDefecto(fechaVencimiento);
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(final String estado) {
		this.estado = ObjectHelper.obtenerValorDefecto(estado, ESTADO_ACTIVO).trim();
	}

}
