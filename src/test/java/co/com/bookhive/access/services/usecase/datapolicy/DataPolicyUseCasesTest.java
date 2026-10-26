package co.com.bookhive.access.services.usecase.datapolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.domain.datapolicy.acceptdatapolicy.AcceptDataPolicyDomain;
import co.com.bookhive.access.domain.datapolicy.finddatapolicy.DataPolicyDomain;
import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.services.usecase.datapolicy.acceptdatapolicy.impl.AcceptDataPolicyUseCaseImpl;
import co.com.bookhive.access.services.usecase.datapolicy.finddatapolicy.impl.FindDataPolicyUseCaseImpl;
import co.com.bookhive.access.testdata.FakeAccess;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class DataPolicyUseCasesTest {

	private static final UUID PERSON = UUIDHelper.generateNew();
	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	private final FakeAccess.Policies policies = new FakeAccess.Policies().with(1, "Versión uno").with(2, "Versión dos");
	private final FakeAccess.Acceptances acceptances = new FakeAccess.Acceptances().currentVersion(2);
	private final List<String> audited = new ArrayList<>();

	@Test
	@DisplayName("muestra la versión vigente de la política y que todavía no la ha aceptado")
	void findsCurrentPolicyNotAccepted() {
		final DataPolicyDomain policy = find().execute();

		assertEquals(2, policy.version());
		assertEquals("Versión dos", policy.content());
		assertFalse(policy.accepted());
		assertEquals(2, acceptances.searches().get(0).version());
	}

	@Test
	@DisplayName("aceptar deja la constancia de quién aceptó qué versión, y la consulta ya la muestra aceptada")
	void acceptsCurrentPolicy() {
		final DataPolicyDomain accepted = accept().execute(new AcceptDataPolicyDomain(2));

		assertTrue(accepted.accepted());
		assertEquals(Instant.parse("2026-10-26T21:00:00Z"), accepted.acceptedAt());
		assertEquals(PERSON, acceptances.created().get(0).person());
		assertEquals(2, acceptances.created().get(0).version());
		assertEquals(List.of(AuditPort.DATA_POLICY_ACCEPTED + " " + INSTITUTION + " " + PERSON + " versión 2"), audited);
		assertTrue(find().execute().accepted());
	}

	@Test
	@DisplayName("aceptar dos veces no duplica la constancia ni la auditoría y conserva la fecha original")
	void acceptingTwiceKeepsOriginal() {
		acceptances.with(PERSON, 2);

		final DataPolicyDomain accepted = accept().execute(new AcceptDataPolicyDomain(2));

		assertEquals(Instant.parse("2026-10-02T15:00:00Z"), accepted.acceptedAt());
		assertTrue(acceptances.created().isEmpty());
		assertTrue(audited.isEmpty());
	}

	@Test
	@DisplayName("no se acepta una versión vieja ni una sin número")
	void rejectsOutdatedOrMissingVersion() {
		final AcceptDataPolicyUseCaseImpl useCase = accept();
		final AcceptDataPolicyDomain outdated = new AcceptDataPolicyDomain(1);

		final BookHiveException old = assertThrows(BookHiveException.class, () -> useCase.execute(outdated));
		final BookHiveException missing = assertThrows(BookHiveException.class, () -> useCase.execute(null));

		assertEquals(BookHiveException.Kind.STATE_DOES_NOT_ALLOW_IT, old.getKind());
		assertEquals(MessageKey.DATA_POLICY_VERSION_OUTDATED, old.getUserMessageKey());
		assertEquals(MessageKey.DATA_POLICY_VERSION_REQUIRED, missing.getUserMessageKey());
		assertTrue(acceptances.created().isEmpty());
	}

	@Test
	@DisplayName("una institución sin política publicada es un error inesperado")
	void institutionWithoutPolicy() {
		final FindDataPolicyUseCaseImpl useCase = new FindDataPolicyUseCaseImpl(
				() -> new ResolveSessionDomain(PERSON, INSTITUTION), new FakeAccess.Policies(), acceptances);

		final BookHiveException exception = assertThrows(BookHiveException.class, useCase::execute);

		assertEquals(BookHiveException.Kind.UNEXPECTED, exception.getKind());
		assertEquals(MessageKey.DATA_POLICY_NOT_PUBLISHED, exception.getUserMessageKey());
	}

	@Test
	@DisplayName("si la constancia no queda guardada no responde como aceptada")
	void failsWhenAcceptanceIsNotStored() {
		final AcceptDataPolicyUseCaseImpl useCase = new AcceptDataPolicyUseCaseImpl(
				() -> new ResolveSessionDomain(PERSON, INSTITUTION), policies, new FakeAccess.Acceptances() {
					@Override
					public void create(final co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyAcceptanceEntity entity) {
						created().add(entity);
					}
				}.currentVersion(2), (a, i, p, d) -> audited.add(a));
		final AcceptDataPolicyDomain acceptance = new AcceptDataPolicyDomain(2);

		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> useCase.execute(acceptance)).getKind());
	}

	private FindDataPolicyUseCaseImpl find() {
		return new FindDataPolicyUseCaseImpl(() -> new ResolveSessionDomain(PERSON, INSTITUTION), policies, acceptances);
	}

	private AcceptDataPolicyUseCaseImpl accept() {
		return new AcceptDataPolicyUseCaseImpl(() -> new ResolveSessionDomain(PERSON, INSTITUTION), policies, acceptances,
				(action, institution, person, detail) -> audited.add(action + " " + institution + " " + person + " " + detail));
	}

}
