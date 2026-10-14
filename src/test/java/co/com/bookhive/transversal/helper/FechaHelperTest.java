package co.com.bookhive.transversal.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FechaHelperTest {

	private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

	@Test
	@DisplayName("a las 7 p.m. de Bogotá en UTC ya es el día siguiente")
	void aLasSieteDeLaNocheEnBogotaEnUtcYaEsElDiaSiguiente() {

		final Instant instante = ZonedDateTime
				.of(2026, 10, 14, 19, 0, 0, 0, BOGOTA)
				.toInstant();

		assertThat(LocalDate.ofInstant(instante, BOGOTA))
				.isEqualTo(LocalDate.of(2026, 10, 14));

		assertThat(FechaHelper.hoyEnUtc(Clock.fixed(instante, BOGOTA)))
				.isEqualTo(LocalDate.of(2026, 10, 15));
	}

	@Test
	@DisplayName("el resultado no depende de la zona del reloj que recibe")
	void elResultadoNoDependeDeLaZonaDelReloj() {

		final Instant instante = Instant.parse("2026-10-14T23:30:00Z");

		assertThat(FechaHelper.hoyEnUtc(Clock.fixed(instante, BOGOTA)))
				.isEqualTo(FechaHelper.hoyEnUtc(Clock.fixed(instante, ZoneOffset.UTC)))
				.isEqualTo(LocalDate.of(2026, 10, 14));
	}

	@Test
	@DisplayName("sin reloj usa el reloj del sistema en UTC")
	void sinRelojUsaElRelojDelSistemaEnUtc() {

		assertThat(FechaHelper.hoyEnUtc())
				.isEqualTo(LocalDate.now(ZoneOffset.UTC));
	}

	@Test
	@DisplayName("un reloj nulo cae al reloj del sistema en UTC")
	void unRelojNuloCaeAlRelojDelSistema() {

		assertThat(FechaHelper.hoyEnUtc(null))
				.isEqualTo(LocalDate.now(ZoneOffset.UTC));
	}

}
