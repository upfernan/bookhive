package co.com.bookhive.access.infrastructure.output.externalservices.provisioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.com.bookhive.crosscutting.exception.BookHiveException;
import co.com.bookhive.crosscutting.messages.MessageKey;

class FlywayInstitutionProvisioningAdapterTest {

	@Test
	@DisplayName("el esquema y el nombre de la universidad nueva salen del dominio")
	void namesFromDomain() {
		assertEquals("upb_edu_co", FlywayInstitutionProvisioningAdapter.schemaOf("upb.edu.co"));
		assertEquals("i_3m_edu", FlywayInstitutionProvisioningAdapter.schemaOf("3m.edu"));
		assertEquals("u_ca_edu_co", FlywayInstitutionProvisioningAdapter.schemaOf("u-ca.edu.co"));
		assertEquals(60, FlywayInstitutionProvisioningAdapter.schemaOf("a".repeat(80) + ".edu").length());
		assertEquals("UPB", FlywayInstitutionProvisioningAdapter.nameOf("upb.edu.co"));
	}

	@Test
	@DisplayName("sin el usuario que crea instituciones no intenta nada y avisa que no está disponible")
	void requiresProvisioningUser() {
		final FlywayInstitutionProvisioningAdapter adapter =
				new FlywayInstitutionProvisioningAdapter("jdbc:postgresql://127.0.0.1:1/bookhive", "", "", "bookhive_app");
		final FlywayInstitutionProvisioningAdapter unreachable =
				new FlywayInstitutionProvisioningAdapter("jdbc:postgresql://127.0.0.1:1/bookhive", "prov", "clave", "bookhive_app");

		final BookHiveException exception = assertThrows(BookHiveException.class, () -> adapter.provision("upb.edu.co"));
		assertEquals(MessageKey.REGISTRATION_INSTITUTION_UNAVAILABLE, exception.getUserMessageKey());
		assertEquals(BookHiveException.Kind.UNAVAILABLE, exception.getKind());
		assertEquals(MessageKey.REGISTRATION_INSTITUTION_UNAVAILABLE,
				assertThrows(BookHiveException.class, () -> unreachable.provision("upb.edu.co")).getUserMessageKey());
	}

}
