package co.com.bookhive.initializer.security;

public final class InternalPaths {

	public static final String TASKS = "/api/v1/tareas/**";
	public static final String NOTICES = "/api/v1/avisos/**";

	private InternalPaths() {
		super();
	}

	public static String[] all() {
		return new String[] { TASKS, NOTICES };
	}

}
