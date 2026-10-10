package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.transversal.helper.FechaHelper;
import co.com.bookhive.transversal.helper.ObjectHelper;
import co.com.bookhive.transversal.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Representa la tabla "multa". La tarifa diaria se guarda fija el día en que
// nace la multa (RT-06): si la institución cambia la tarifa después, esta
// multa no se recalcula.
@Entity
@Table(name = "multa")
public class MultaEntidad {

	public static final String ESTADO_PENDIENTE = "PENDIENTE";
	public static final String ESTADO_PAGADA = "PAGADA";

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID identificador;

	@Column(name = "prestamo_id", nullable = false, updatable = false)
	private UUID prestamo;

	@Column(name = "usuario_id", nullable = false, updatable = false)
	private UUID usuario;

	@Column(name = "fecha_generacion", nullable = false, updatable = false)
	private LocalDate fechaGeneracion;

	@Column(name = "dias_retraso", nullable = false, updatable = false)
	private int diasRetraso;

	@Column(name = "tarifa_diaria", nullable = false, updatable = false, precision = 12, scale = 2)
	private BigDecimal tarifaDiaria;

	@Column(name = "valor_total", nullable = false, updatable = false, precision = 12, scale = 2)
	private BigDecimal valorTotal;

	@Column(name = "estado", nullable = false, length = 20)
	private String estado;

	public MultaEntidad() {
		super();
		setIdentificador(UUIDHelper.obtenerValorDefecto());
		setPrestamo(UUIDHelper.obtenerValorDefecto());
		setUsuario(UUIDHelper.obtenerValorDefecto());
		setFechaGeneracion(FechaHelper.obtenerValorDefecto());
		setDiasRetraso(0);
		setTarifaDiaria(BigDecimal.ZERO);
		setValorTotal(BigDecimal.ZERO);
		setEstado(ESTADO_PENDIENTE);
	}

	public UUID getIdentificador() {
		return identificador;
	}

	public void setIdentificador(final UUID identificador) {
		this.identificador = UUIDHelper.obtenerValorDefecto(identificador);
	}

	public UUID getPrestamo() {
		return prestamo;
	}

	public void setPrestamo(final UUID prestamo) {
		this.prestamo = UUIDHelper.obtenerValorDefecto(prestamo);
	}

	public UUID getUsuario() {
		return usuario;
	}

	public void setUsuario(final UUID usuario) {
		this.usuario = UUIDHelper.obtenerValorDefecto(usuario);
	}

	public LocalDate getFechaGeneracion() {
		return fechaGeneracion;
	}

	public void setFechaGeneracion(final LocalDate fechaGeneracion) {
		this.fechaGeneracion = FechaHelper.obtenerValorDefecto(fechaGeneracion);
	}

	public int getDiasRetraso() {
		return diasRetraso;
	}

	public void setDiasRetraso(final int diasRetraso) {
		this.diasRetraso = Math.max(diasRetraso, 0);
	}

	public BigDecimal getTarifaDiaria() {
		return tarifaDiaria;
	}

	public void setTarifaDiaria(final BigDecimal tarifaDiaria) {
		this.tarifaDiaria = ObjectHelper.obtenerValorDefecto(tarifaDiaria, BigDecimal.ZERO);
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(final BigDecimal valorTotal) {
		this.valorTotal = ObjectHelper.obtenerValorDefecto(valorTotal, BigDecimal.ZERO);
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(final String estado) {
		this.estado = ObjectHelper.obtenerValorDefecto(estado, ESTADO_PENDIENTE).trim();
	}

}
