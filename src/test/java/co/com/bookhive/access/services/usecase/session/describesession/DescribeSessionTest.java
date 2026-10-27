package co.com.bookhive.access.services.usecase.session.describesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.access.domain.session.resolvesession.ResolveSessionDomain;
import co.com.bookhive.access.infrastructure.input.controllers.SessionProfileController;
import co.com.bookhive.access.infrastructure.output.repositories.structured.adapters.SessionProfileRepositoryAdapter;
import co.com.bookhive.access.services.input.adapters.DescribeSessionInputAdapter;
import co.com.bookhive.access.services.input.dto.response.SessionProfileResponseDTO;
import co.com.bookhive.access.services.output.ports.repositories.entities.SessionProfileEntity;
import co.com.bookhive.access.services.usecase.session.describesession.impl.DescribeSessionUseCaseImpl;
import co.com.bookhive.access.testdata.FakeAccess;
import co.com.bookhive.access.testdata.FakeDataSource;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.helper.UUIDHelper;
import co.com.bookhive.crosscutting.security.PermissionCode;
import co.com.bookhive.crosscutting.tenancy.TenantContext;

class DescribeSessionTest {

	private static final UUID PERSON = UUIDHelper.generateNew();
	private static final UUID INSTITUTION = UUIDHelper.generateNew();

	@AfterEach
	void clear() {
		TenantContext.clear();
	}

	@Test
	@DisplayName("le dice al front quién está en sesión, de qué institución, con qué roles y qué permisos")
	void describesSession() {
		final FakeAccess.Permissions permissions = new FakeAccess.Permissions()
				.with(FakeAccess.permission(PermissionCode.VIEW_ACCOUNT))
				.with(FakeAccess.permission(PermissionCode.LEND_BOOK))
				.with(FakeAccess.permission(PermissionCode.LEND_BOOK));
		final SessionProfileController controller = new SessionProfileController(new DescribeSessionInputAdapter(
				new DescribeSessionUseCaseImpl(() -> new ResolveSessionDomain(PERSON, INSTITUTION),
						(person, institution) -> new SessionProfileEntity("  ana@uco.edu.co ", "UCO", List.of("LECTOR", "BIBLIOTECARIO")),
						permissions)));

		final SessionProfileResponseDTO profile = controller.current().getBody();

		assertEquals(new SessionProfileResponseDTO("ana@uco.edu.co", "UCO", List.of("BIBLIOTECARIO", "LECTOR"),
				List.of(PermissionCode.VIEW_ACCOUNT, PermissionCode.LEND_BOOK)), profile);
		assertEquals(PERSON, permissions.searches().get(0).getPerson());
		assertTrue(permissions.searches().get(0).getCode().isEmpty());
	}

	@Test
	@DisplayName("el adaptador lee el perfil y responde error inesperado si la base falla")
	void adapterReadsProfile() {
		TenantContext.set("uco");

		final SessionProfileEntity found = new SessionProfileRepositoryAdapter(FakeDataSource.withoutRows())
				.findProfile(PERSON, INSTITUTION);
		final SessionProfileRepositoryAdapter failing = new SessionProfileRepositoryAdapter(FakeDataSource.failing());

		assertEquals(new SessionProfileEntity("", "", List.of()), found);
		assertEquals(BookHiveException.Kind.UNEXPECTED,
				assertThrows(BookHiveException.class, () -> failing.findProfile(PERSON, INSTITUTION)).getKind());
	}

}
