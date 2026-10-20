package co.com.bookhive.gateway.testdata;

import java.util.ArrayList;
import java.util.List;

import co.com.bookhive.gateway.services.output.ports.AuditPort;

public final class RecordingAudit implements AuditPort {

	private final List<String> events = new ArrayList<>();

	@Override
	public void register(final String action, final String account, final String origin, final String detail) {
		events.add(action + "|" + account + "|" + origin + "|" + detail);
	}

	public List<String> events() {
		return List.copyOf(events);
	}

	public List<String> actions() {
		return events.stream().map(event -> event.substring(0, event.indexOf('|'))).toList();
	}

}
