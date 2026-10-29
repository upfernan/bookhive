package co.com.bookhive.access.testdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import co.com.bookhive.access.services.output.ports.IdentityProviderPort;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyAcceptanceRepository;
import co.com.bookhive.access.services.output.ports.repositories.DataPolicyRepository;
import co.com.bookhive.access.services.output.ports.repositories.InstitutionRepository;
import co.com.bookhive.access.services.output.ports.repositories.PermissionRepository;
import co.com.bookhive.access.services.output.ports.repositories.PersonRepository;
import co.com.bookhive.access.services.output.ports.repositories.criteria.DataPolicyAcceptanceCriteria;
import co.com.bookhive.access.services.output.ports.repositories.criteria.InstitutionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.criteria.PermissionCriteria;
import co.com.bookhive.access.services.output.ports.repositories.criteria.PersonCriteria;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyAcceptanceEntity;
import co.com.bookhive.access.services.output.ports.repositories.entities.DataPolicyEntity;
import co.com.bookhive.access.services.output.ports.repositories.entities.InstitutionEntity;
import co.com.bookhive.access.services.output.ports.repositories.entities.PermissionEntity;
import co.com.bookhive.access.services.output.ports.repositories.entities.PersonEntity;

public final class FakeAccess {

	private FakeAccess() {
		super();
	}

	public static PersonEntity person(final UUID id, final UUID institution) {
		final PersonEntity person = new PersonEntity();
		person.setId(id);
		person.setInstitution(institution);
		return person;
	}

	public static InstitutionEntity institution(final UUID id, final String schema) {
		final InstitutionEntity institution = new InstitutionEntity();
		institution.setId(id);
		institution.setSchema(schema);
		return institution;
	}

	public static PermissionEntity permission(final String code) {
		final PermissionEntity permission = new PermissionEntity();
		permission.setCode(code);
		return permission;
	}

	public static final class People implements PersonRepository {

		private final List<PersonEntity> people = new ArrayList<>();
		private final List<PersonCriteria> searches = new ArrayList<>();

		public People with(final PersonEntity person) {
			people.add(person);
			return this;
		}

		public List<PersonCriteria> searches() {
			return searches;
		}

		@Override
		public List<PersonEntity> findByCriteria(final PersonCriteria criteria) {
			searches.add(criteria);
			return List.copyOf(people);
		}

	}

	public static final class Institutions implements InstitutionRepository {

		private final List<InstitutionEntity> institutions = new ArrayList<>();
		private final List<InstitutionCriteria> searches = new ArrayList<>();

		public Institutions with(final InstitutionEntity institution) {
			institutions.add(institution);
			return this;
		}

		public List<InstitutionCriteria> searches() {
			return searches;
		}

		@Override
		public List<InstitutionEntity> findByCriteria(final InstitutionCriteria criteria) {
			searches.add(criteria);
			return List.copyOf(institutions);
		}

	}

	public static final class Permissions implements PermissionRepository {

		private final List<PermissionEntity> permissions = new ArrayList<>();
		private final List<PermissionCriteria> searches = new ArrayList<>();

		public Permissions with(final PermissionEntity permission) {
			permissions.add(permission);
			return this;
		}

		public List<PermissionCriteria> searches() {
			return searches;
		}

		@Override
		public List<PermissionEntity> findByCriteria(final PermissionCriteria criteria) {
			searches.add(criteria);
			return List.copyOf(permissions);
		}

	}

	public static final class Policies implements DataPolicyRepository {

		private final List<DataPolicyEntity> policies = new ArrayList<>();

		public Policies with(final int version, final String content) {
			policies.add(new DataPolicyEntity(version, content, java.time.Instant.parse("2026-10-01T05:00:00Z")));
			return this;
		}

		@Override
		public Optional<DataPolicyEntity> findCurrent() {
			return policies.stream().max(java.util.Comparator.comparingInt(DataPolicyEntity::version));
		}

	}

	public static class Acceptances implements DataPolicyAcceptanceRepository {

		private final List<DataPolicyAcceptanceEntity> stored = new ArrayList<>();
		private final List<DataPolicyAcceptanceCriteria> searches = new ArrayList<>();
		private final List<DataPolicyAcceptanceEntity> created = new ArrayList<>();
		private int currentVersion = 1;

		public Acceptances currentVersion(final int version) {
			currentVersion = version;
			return this;
		}

		public Acceptances with(final UUID person, final int version) {
			stored.add(new DataPolicyAcceptanceEntity(person, version, java.time.Instant.parse("2026-10-02T15:00:00Z")));
			return this;
		}

		public List<DataPolicyAcceptanceCriteria> searches() {
			return searches;
		}

		public List<DataPolicyAcceptanceEntity> created() {
			return created;
		}

		@Override
		public void create(final DataPolicyAcceptanceEntity entity) {
			created.add(entity);
			stored.add(new DataPolicyAcceptanceEntity(entity.person(), entity.version(),
					java.time.Instant.parse("2026-10-26T21:00:00Z")));
		}

		@Override
		public List<DataPolicyAcceptanceEntity> findByCriteria(final DataPolicyAcceptanceCriteria criteria) {
			searches.add(criteria);
			final int version = criteria.meansCurrentPolicy() ? currentVersion : criteria.version();
			return stored.stream()
					.filter(acceptance -> acceptance.person().equals(criteria.person()) && acceptance.version() == version)
					.toList();
		}

	}

	public static final class IdentityProvider implements IdentityProviderPort {

		private final String identity;
		private final List<String> receivedIdTokens = new ArrayList<>();
		private final List<String> closedSessions = new ArrayList<>();

		public IdentityProvider(final String identity) {
			this.identity = identity;
		}

		public List<String> receivedIdTokens() {
			return receivedIdTokens;
		}

		public List<String> closedSessions() {
			return closedSessions;
		}

		@Override
		public co.com.bookhive.access.services.output.ports.entities.VerifiedIdentityEntity verify(final String idToken) {
			receivedIdTokens.add(idToken);
			return new co.com.bookhive.access.services.output.ports.entities.VerifiedIdentityEntity(
					identity, identity + "@uco.edu.co", true, "cookie-de-" + idToken);
		}

		@Override
		public String openSession(final String idToken) {
			receivedIdTokens.add(idToken);
			return "cookie-de-sesion";
		}

		@Override
		public String identityOf(final String sessionToken) {
			return identity;
		}

		@Override
		public void closeSession(final String sessionToken) {
			closedSessions.add(sessionToken);
		}

	}

}
