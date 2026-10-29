package co.com.bookhive.access.services.usecase.session.registerperson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import co.com.bookhive.access.domain.datapolicy.findpublicdatapolicy.PublicDataPolicyDomain;
import co.com.bookhive.access.domain.session.registerperson.RegistrationDomain;
import co.com.bookhive.access.services.input.adapters.FindPublicDataPolicyInputAdapter;
import co.com.bookhive.access.services.input.adapters.RegisterPersonInputAdapter;
import co.com.bookhive.access.services.input.dto.request.RegisterPersonRequestDTO;
import co.com.bookhive.access.services.input.dto.response.PublicDataPolicyResponseDTO;
import co.com.bookhive.access.services.output.ports.IdentityProviderPort;
import co.com.bookhive.access.services.output.ports.entities.VerifiedIdentityEntity;
import co.com.bookhive.access.services.output.ports.repositories.RegistrationRepository;
import co.com.bookhive.access.services.output.ports.repositories.entities.RegistrationEntities;
import co.com.bookhive.access.services.usecase.datapolicy.findpublicdatapolicy.impl.FindPublicDataPolicyUseCaseImpl;
import co.com.bookhive.access.services.usecase.session.registerperson.impl.RegisterPersonUseCaseImpl;
import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;
import co.com.bookhive.shared.services.output.ports.AuditPort;

class RegistrationTest {

	private static final RegistrationEntities.DomainInstitution UCO =
			new RegistrationEntities.DomainInstitution(UUID.randomUUID(), "uco");
	private static final Instant PUBLISHED = Instant.parse("2026-10-20T15:04:05Z");

	private final Registrations registrations = new Registrations();
	private final List<String> audited = new ArrayList<>();
	private final List<String> provisioned = new ArrayList<>();
	private final AuditPort audit = (action, institution, person, detail) -> audited.add(action + " " + detail);

	@Test
	@DisplayName("con el correo verificado de un dominio registrado crea la persona y guarda la versión aceptada")
	void registersVerifiedPerson() {
		final RegistrationDomain.Registered registered =
				useCase(identity("carla@UCO.edu.co", true)).execute(new RegistrationDomain("token", " Carla Gómez ", 1));

		final RegistrationEntities.NewPerson person = registrations.registered.get(0);
		assertEquals("carla-id", person.identity());
		assertEquals("Carla Gómez", person.name());
		assertEquals(1, person.policyVersion());
		assertEquals("uco", registered.schema());
		assertEquals("cookie-carla", registered.sessionToken());
		assertEquals(List.of(AuditPort.PERSON_REGISTERED + " uco.edu.co"), audited);
	}

	@Test
	@DisplayName("el adaptador de entrada entrega la versión del aviso y la cookie de la sesión")
	void inputAdapterPassesPolicyVersion() {
		final String token = new RegisterPersonInputAdapter(useCase(identity("carla@uco.edu.co", true)))
				.execute(new RegisterPersonRequestDTO("token", "Carla", 1)).sessionToken();

		assertEquals("cookie-carla", token);
		assertEquals(1, registrations.registered.get(0).policyVersion());
	}

	@Test
	@DisplayName("sin aceptar el aviso, sin nombre o sin pase no registra")
	void rejectsIncompleteRequests() {
		final RegisterPersonUseCaseImpl useCase = useCase(identity("carla@uco.edu.co", true));
		final RegistrationDomain withoutPolicy = new RegistrationDomain("token", "Carla", 0);
		final RegistrationDomain withoutName = new RegistrationDomain("token", " ", 1);

		assertEquals(MessageKey.DATA_POLICY_VERSION_REQUIRED,
				assertThrows(BookHiveException.class, () -> useCase.execute(withoutPolicy)).getUserMessageKey());
		assertEquals(MessageKey.REGISTRATION_DATA_INVALID,
				assertThrows(BookHiveException.class, () -> useCase.execute(withoutName)).getUserMessageKey());
		assertEquals(MessageKey.REGISTRATION_DATA_INVALID,
				assertThrows(BookHiveException.class, () -> useCase.execute(null)).getUserMessageKey());
		assertTrue(registrations.registered.isEmpty());
	}

	@Test
	@DisplayName("no registra correos sin verificar, dominios ajenos ni identidades ya registradas")
	void rejectsUnverifiedForeignOrRepeated() {
		final RegistrationDomain request = new RegistrationDomain("token", "Carla", 1);
		final RegisterPersonUseCaseImpl unverified = useCase(identity("carla@uco.edu.co", false));
		final RegisterPersonUseCaseImpl foreign = useCase(identity("carla@empresa.com", true));
		final RegisterPersonUseCaseImpl repeated = useCase(identity("carla@uco.edu.co", true));
		registrations.known.add("carla-id");

		assertEquals(MessageKey.REGISTRATION_EMAIL_NOT_VERIFIED,
				assertThrows(BookHiveException.class, () -> unverified.execute(request)).getUserMessageKey());
		assertEquals(MessageKey.REGISTRATION_ALREADY_REGISTERED,
				assertThrows(BookHiveException.class, () -> repeated.execute(request)).getUserMessageKey());
		registrations.known.clear();
		final BookHiveException foreignError = assertThrows(BookHiveException.class, () -> foreign.execute(request));
		assertEquals(MessageKey.REGISTRATION_DOMAIN_NOT_ALLOWED, foreignError.getUserMessageKey());
		assertEquals(BookHiveException.Kind.NOT_ALLOWED, foreignError.getKind());
		assertTrue(registrations.registered.isEmpty());
	}

	@Test
	@DisplayName("el aviso público sale del dominio con su versión, texto y fecha")
	void findsPublicPolicyByDomain() {
		final PublicDataPolicyResponseDTO policy =
				new FindPublicDataPolicyInputAdapter(new FindPublicDataPolicyUseCaseImpl(registrations)).execute(" UCO.edu.co ");

		assertEquals(3, policy.version());
		assertEquals("Aviso de la UCO", policy.content());
		assertEquals(PUBLISHED, policy.publishedAt());
		assertEquals(List.of("uco.edu.co"), registrations.domainsAsked);
	}

	@Test
	@DisplayName("un dominio inválido, no registrado o sin aviso responde que no se encontró, sin decir cuál fue el caso")
	void publicPolicyNotFound() {
		final FindPublicDataPolicyUseCaseImpl find = new FindPublicDataPolicyUseCaseImpl(registrations);
		registrations.withoutPolicy = true;

		for (final String domain : new String[] { null, "", "carla@uco.edu.co", "uco", "gmail.com", "uco.edu.co" }) {
			final BookHiveException exception = assertThrows(BookHiveException.class, () -> find.execute(domain));
			assertEquals(MessageKey.DATA_POLICY_PUBLIC_NOT_FOUND, exception.getUserMessageKey());
			assertEquals(BookHiveException.Kind.NOT_FOUND, exception.getKind());
		}
		assertEquals(List.of("gmail.com", "uco.edu.co"), registrations.domainsAsked);
	}

	@Test
	@DisplayName("un correo de una universidad que aún no existe crea su biblioteca y registra a la persona allí")
	void provisionsNewEducationalInstitution() {
		final RegistrationDomain.Registered registered =
				useCase(identity("ana@upb.edu.co", true)).execute(new RegistrationDomain("token", "Ana", 1));

		assertEquals(List.of("upb.edu.co"), provisioned);
		assertEquals("upb_edu_co", registered.schema());
		assertEquals(AuditPort.INSTITUTION_SELF_REGISTERED + " upb.edu.co -> upb_edu_co", audited.get(0));
		assertEquals(AuditPort.PERSON_REGISTERED + " upb.edu.co", audited.get(1));
	}

	@Test
	@DisplayName("solo los dominios educativos crean una biblioteca nueva")
	void recognizesEducationalDomains() {
		for (final String domain : new String[] { "upb.edu.co", "eafit.edu.co", "mit.edu", "ox.ac.uk", "unam.edu.mx" }) {
			assertTrue(RegistrationDomain.isEducational(domain), domain);
		}
		for (final String domain : new String[] { "gmail.com", "edu.co", "edu", "empresa.com.co", "uco.net.co", "", "ac.uk" }) {
			assertTrue(!RegistrationDomain.isEducational(domain), domain);
		}
	}

	@Test
	@DisplayName("una universidad que aún no existe muestra el aviso base; un dominio no educativo no muestra nada")
	void defaultPolicyForNewUniversities() {
		final FindPublicDataPolicyUseCaseImpl find = new FindPublicDataPolicyUseCaseImpl(registrations);

		assertEquals("Aviso base", find.execute("eafit.edu.co").content());
		assertEquals(1, find.execute("eafit.edu.co").version());
		assertEquals(MessageKey.DATA_POLICY_PUBLIC_NOT_FOUND,
				assertThrows(BookHiveException.class, () -> find.execute("empresa.com")).getUserMessageKey());
	}

	@Test
	@DisplayName("acepta subdominios y rechaza dominios mal escritos")
	void validatesDomainFormat() {
		assertEquals("correo.uco.edu.co", PublicDataPolicyDomain.normalizedDomain("Correo.UCO.edu.co"));
		assertThrows(BookHiveException.class, () -> PublicDataPolicyDomain.normalizedDomain("-uco.edu.co"));
		assertThrows(BookHiveException.class, () -> PublicDataPolicyDomain.normalizedDomain("uco..edu.co"));
		assertEquals("", new PublicDataPolicyDomain(1, null, PUBLISHED).content());
	}

	private RegisterPersonUseCaseImpl useCase(final VerifiedIdentityEntity identity) {
		return new RegisterPersonUseCaseImpl(new IdentityProviderPort() {

			@Override
			public String openSession(final String idToken) {
				return "";
			}

			@Override
			public VerifiedIdentityEntity verify(final String idToken) {
				return identity;
			}

			@Override
			public String identityOf(final String sessionToken) {
				return "";
			}

			@Override
			public void closeSession(final String sessionToken) {
				throw new UnsupportedOperationException();
			}

		}, registrations, domain -> {
			provisioned.add(domain);
			return new RegistrationEntities.DomainInstitution(UUID.randomUUID(), domain.replace('.', '_'));
		}, audit);
	}

	private static VerifiedIdentityEntity identity(final String email, final boolean verified) {
		return new VerifiedIdentityEntity("carla-id", email, verified, "cookie-carla");
	}

	private static final class Registrations implements RegistrationRepository {

		private final List<RegistrationEntities.NewPerson> registered = new ArrayList<>();
		private final List<String> known = new ArrayList<>();
		private final List<String> domainsAsked = new ArrayList<>();
		private boolean withoutPolicy;

		@Override
		public Optional<RegistrationEntities.DomainInstitution> institutionOfDomain(final String domain) {
			domainsAsked.add(domain);
			return "uco.edu.co".equals(domain) ? Optional.of(UCO) : Optional.empty();
		}

		@Override
		public boolean identityRegistered(final String identity) {
			return known.contains(identity);
		}

		@Override
		public Optional<RegistrationEntities.PublicPolicy> currentPolicyOf(final RegistrationEntities.DomainInstitution institution) {
			return withoutPolicy ? Optional.empty() : Optional.of(new RegistrationEntities.PublicPolicy(3, "Aviso de la UCO", PUBLISHED));
		}

		@Override
		public Optional<RegistrationEntities.PublicPolicy> defaultPolicy() {
			return Optional.of(new RegistrationEntities.PublicPolicy(1, "Aviso base", null));
		}

		@Override
		public RegistrationEntities.NewPerson register(
				final RegistrationEntities.NewPerson person, final RegistrationEntities.DomainInstitution institution) {
			registered.add(person);
			return person;
		}

	}

}
