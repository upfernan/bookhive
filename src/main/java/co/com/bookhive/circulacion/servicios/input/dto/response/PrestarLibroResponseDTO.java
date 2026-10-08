package co.com.bookhive.circulacion.servicios.input.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import co.com.bookhive.transversal.helper.FechaHelper;
import co.com.bookhive.transversal.helper.UUIDHelper;

public final class PrestarLibroResponseDTO {

	private UUID prestamo;
	private LocalDate fechaVencimiento;

	public PrestarLibroResponseDTO() {
		super();
		setPrestamo(UUIDHelper.obtenerValorDefecto());
		setFechaVencimiento(FechaHelper.obtenerValorDefecto());
	}

	public PrestarLibroResponseDTO(final UUID prestamo, final LocalDate fechaVencimiento) {
		super();
		setPrestamo(prestamo);
		setFechaVencimiento(fechaVencimiento);
	}

	public UUID getPrestamo() {
		return prestamo;
	}

	public LocalDate getFechaVencimiento() {
		return fechaVencimiento;
	}

	private void setPrestamo(final UUID prestamo) {
		this.prestamo = UUIDHelper.obtenerValorDefecto(prestamo);
	}

	private void setFechaVencimiento(final LocalDate fechaVencimiento) {
		this.fechaVencimiento = FechaHelper.obtenerValorDefecto(fechaVencimiento);
	}

}
