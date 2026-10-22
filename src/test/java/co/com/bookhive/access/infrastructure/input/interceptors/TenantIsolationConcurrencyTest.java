package co.com.bookhive.access.infrastructure.input.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import co.com.bookhive.access.infrastructure.output.externalservices.identity.SecurityContextIdentityAdapter;
import co.com.bookhive.access.infrastructure.output.repositories.structured.tenancy.TenantSchemaResolver;
import co.com.bookhive.access.services.input.adapters.ResolveSchemaInputAdapter;
import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;
import co.com.bookhive.access.services.usecase.session.resolveschema.impl.ResolveSchemaUseCaseImpl;
import co.com.bookhive.access.services.usecase.session.resolvesession.impl.ResolveSessionUseCaseImpl;
import co.com.bookhive.access.testdata.FakeAccess;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

class TenantIsolationConcurrencyTest {

	private static final int REQUESTS = 400;

	private static final Institution UCO = new Institution("uid-uco", UUIDHelper.generateNew(), "uco");
	private static final Institution UNAL = new Institution("uid-unal", UUIDHelper.generateNew(), "unal");

	private final TenantSchemaResolver schemaResolver = new TenantSchemaResolver();
	private final TenantInterceptor interceptor = new TenantInterceptor(new ResolveSchemaInputAdapter(
			new ResolveSchemaUseCaseImpl(
					new ResolveSessionUseCaseImpl(new SecurityContextIdentityAdapter(), criteria ->
							List.of(person(criteria.getIdentity()))),
					criteria -> List.of(institution(criteria.getId())))));

	@Test
	@DisplayName("con muchas peticiones al tiempo de dos instituciones, ninguna ve el esquema de la otra")
	void concurrentRequestsNeverSeeAnotherSchema() throws InterruptedException {
		final ConcurrentLinkedQueue<String> mistakes = new ConcurrentLinkedQueue<>();
		final CountDownLatch start = new CountDownLatch(1);
		final List<Runnable> requests = new ArrayList<>();

		for (int request = 0; request < REQUESTS; request++) {
			final Institution institution = request % 2 == 0 ? UCO : UNAL;
			requests.add(() -> attend(institution, start, mistakes));
		}

		try (ExecutorService virtualThreads = Executors.newVirtualThreadPerTaskExecutor()) {
			requests.forEach(virtualThreads::submit);
			start.countDown();
			virtualThreads.shutdown();
			assertTrue(virtualThreads.awaitTermination(30, TimeUnit.SECONDS));
		}

		assertEquals(List.of(), List.copyOf(mistakes));
	}

	private void attend(final Institution institution, final CountDownLatch start,
			final ConcurrentLinkedQueue<String> mistakes) {

		try {
			start.await();
			SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(pass(institution.identity())));

			interceptor.preHandle(null, null, new Object());
			Thread.yield();
			final String seen = schemaResolver.resolveCurrentTenantIdentifier();

			if (!institution.schema().equals(seen)) {
				mistakes.add(institution.schema() + " vio " + seen);
			}

			interceptor.afterCompletion(null, null, new Object(), null);

			if (!TenantContext.SCHEMA_WITHOUT_TENANT.equals(schemaResolver.resolveCurrentTenantIdentifier())) {
				mistakes.add(institution.schema() + " no limpió su esquema al terminar");
			}
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
			mistakes.add(institution.schema() + " fue interrumpida");
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	private static PersonEntity person(final String identity) {
		final Institution institution = UCO.identity().equals(identity) ? UCO : UNAL;
		return FakeAccess.person(UUIDHelper.generateNew(), institution.id());
	}

	private static InstitutionEntity institution(final UUID id) {
		final Institution institution = UCO.id().equals(id) ? UCO : UNAL;
		return FakeAccess.institution(institution.id(), institution.schema());
	}

	private static Jwt pass(final String identity) {
		final Instant now = Instant.now();
		return new Jwt("pase", now, now.plusSeconds(60), Map.of("alg", "RS256"), Map.of("sub", identity));
	}

	private record Institution(String identity, UUID id, String schema) {
	}

}
