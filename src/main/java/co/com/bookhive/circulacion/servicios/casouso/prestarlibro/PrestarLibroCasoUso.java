package co.com.bookhive.circulacion.servicios.casouso.prestarlibro;

import co.com.bookhive.circulacion.dominio.prestarlibro.PrestarLibroDominio;
import co.com.bookhive.circulacion.servicios.casouso.CasoUso;

// Contrato del caso de uso "prestar libro".
// Recibe el préstamo que se quiere hacer y devuelve el mismo préstamo ya
// validado, guardado y con su fecha de vencimiento calculada.
public interface PrestarLibroCasoUso extends CasoUso<PrestarLibroDominio, PrestarLibroDominio> {

}
