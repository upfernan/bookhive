package co.com.bookhive.gateway.services.output.ports;

public interface AuditPort {

	String FAILED_ATTEMPT = "INTENTO_FALLIDO";
	String ACCOUNT_HELD = "CUENTA_FRENADA";
	String ORIGIN_HELD = "ORIGEN_FRENADO";
	String REQUEST_HELD = "SOLICITUD_FRENADA";

	void register(String action, String account, String origin, String detail);

}
