package co.com.bookhive.circulacion.infraestructura.output.repositorios.estructurados.entidad;

import java.util.UUID;

import co.com.bookhive.transversal.helper.ObjectHelper;
import co.com.bookhive.transversal.helper.UUIDHelper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Representa la tabla "ejemplar": la copia física de un libro en una sede.
// "prestable" en falso significa que el ejemplar es solo de consulta en sala.
@Entity
@Table(name = "ejemplar")
public class EjemplarEntidad {

	public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
	public static final String ESTADO_PRESTADO = "PRESTADO";
	public static final String ESTADO_RESERVADO = "RESERVADO";

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID identificador;

	@Column(name = "libro_id", nullable = false)
	private UUID libro;

	@Column(name = "sede_id", nullable = false)
	private UUID sede;

	@Column(name = "codigo_barras", nullable = false, unique = true, length = 50)
	private String codigoBarras;

	@Column(name = "estado", nullable = false, length = 20)
	private String estado;

	@Column(name = "prestable", nullable = false)
	private boolean prestable;

	public EjemplarEntidad() {
		super();
		setIdentificador(UUIDHelper.obtenerValorDefecto());
		setLibro(UUIDHelper.obtenerValorDefecto());
		setSede(UUIDHelper.obtenerValorDefecto());
		setCodigoBarras("");
		setEstado(ESTADO_DISPONIBLE);
		setPrestable(true);
	}

	public UUID getIdentificador() {
		return identificador;
	}

	public void setIdentificador(final UUID identificador) {
		this.identificador = UUIDHelper.obtenerValorDefecto(identificador);
	}

	public UUID getLibro() {
		return libro;
	}

	public void setLibro(final UUID libro) {
		this.libro = UUIDHelper.obtenerValorDefecto(libro);
	}

	public UUID getSede() {
		return sede;
	}

	public void setSede(final UUID sede) {
		this.sede = UUIDHelper.obtenerValorDefecto(sede);
	}

	public String getCodigoBarras() {
		return codigoBarras;
	}

	public void setCodigoBarras(final String codigoBarras) {
		this.codigoBarras = ObjectHelper.obtenerValorDefecto(codigoBarras, "").trim();
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(final String estado) {
		this.estado = ObjectHelper.obtenerValorDefecto(estado, ESTADO_DISPONIBLE).trim();
	}

	public boolean isPrestable() {
		return prestable;
	}

	public void setPrestable(final boolean prestable) {
		this.prestable = prestable;
	}

}
