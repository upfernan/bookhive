package co.com.bookhive.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class ArchitectureRulesTest {

	private static final JavaClasses BOOKHIVE = new ClassFileImporter()
			.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
			.importPackages("co.com.bookhive");

	private static final String ROOT = "co.com.bookhive.";

	private static final String[] MODULES = { "access", "circulation" };

	private static final String[] FRAMEWORKS = {
			"org.springframework..",
			"jakarta..",
			"org.hibernate..",
			"lombok..",
			"org.mapstruct..",
			"com.fasterxml..",
			"tools.jackson..",
			"io.opentelemetry..",
			"org.flywaydb.." };

	@Test
	@DisplayName("las capas solo se usan de afuera hacia adentro")
	void layersRespectDependencyRule() {
		layeredArchitecture()
				.consideringOnlyDependenciesInLayers()
				.layer("Domain").definedBy("..domain..")
				.layer("Services").definedBy("..services..")
				.layer("Infrastructure").definedBy("..infrastructure..")
				.layer("Initializer").definedBy("..initializer..")
				.whereLayer("Initializer").mayNotBeAccessedByAnyLayer()
				.whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Initializer")
				.whereLayer("Services").mayOnlyBeAccessedByLayers("Domain", "Infrastructure", "Initializer")
				.whereLayer("Domain").mayOnlyBeAccessedByLayers("Services", "Infrastructure", "Initializer")
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("el dominio, los servicios y el crosscutting no dependen de ningún framework")
	void coreDoesNotDependOnFrameworks() {
		noClasses()
				.that().resideInAnyPackage("..domain..", "..services..", "..crosscutting..")
				.should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
				.because("Spring, JPA y MapStruct solo viven en infrastructure e initializer")
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("los puertos de entrada, de salida y de repositorios son interfaces")
	void portsAreInterfaces() {
		classes()
				.that().resideInAnyPackage(
						"..services.input.ports",
						"..services.output.ports",
						"..services.output.ports.repositories")
				.should().beInterfaces()
				.because("todo se comunica por interfaces")
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("los contratos de los casos de uso son interfaces")
	void useCaseContractsAreInterfaces() {
		classes()
				.that().resideInAPackage("..services.usecase..")
				.and().resideOutsideOfPackage("..impl..")
				.should().beInterfaces()
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("las implementaciones de los casos de uso viven en su paquete impl")
	void useCaseImplementationsLiveInImpl() {
		classes()
				.that().haveSimpleNameEndingWith("UseCaseImpl")
				.should().resideInAPackage("..services.usecase..impl..")
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("la infraestructura de entrada solo habla con los puertos de entrada")
	void inputInfrastructureOnlyUsesInputPorts() {
		noClasses()
				.that().resideInAPackage("..infrastructure.input..")
				.should().dependOnClassesThat().resideInAnyPackage(
						"..services.usecase..",
						"..services.input.adapters..",
						"..services.output..",
						"..domain..",
						"..infrastructure.output..")
				.because("controladores e interceptores nunca llaman al caso de uso, al adaptador ni a la salida:"
						+ " llaman al puerto de entrada")
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("los adaptadores de entrada solo llaman a su caso de uso")
	void inputAdaptersOnlyCallUseCases() {
		noClasses()
				.that().resideInAnyPackage(
						ROOT + "access.services.input.adapters..",
						ROOT + "shared.services.input.adapters..")
				.should().dependOnClassesThat().resideInAnyPackage("..services.output..", "..infrastructure..")
				.because("el adaptador de entrada convierte el DTO en dominio y se lo pasa al caso de uso,"
						+ " que es el único que sale por los puertos de salida")
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("los casos de uso trabajan con dominio, nunca con DTO")
	void useCasesDoNotUseDtos() {
		noClasses()
				.that().resideInAPackage("..services.usecase..")
				.should().dependOnClassesThat().resideInAPackage("..services.input.dto..")
				.because("al caso de uso la información le llega como dominio")
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("las entidades JPA solo viven en structured.jpa.entities")
	void jpaEntitiesLiveInStructuredRepositories() {
		classes()
				.that().areAnnotatedWith("jakarta.persistence.Entity")
				.should().resideInAPackage("..infrastructure.output.repositories.structured.jpa.entities..")
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("el crosscutting y lo compartido no dependen de ningún módulo")
	void crosscuttingAndSharedAreIndependent() {
		noClasses()
				.that().resideInAnyPackage(ROOT + "crosscutting..", ROOT + "shared..")
				.should().dependOnClassesThat().resideInAnyPackage(modulePackages(ROOT + "initializer.."))
				.because("lo compartido lo usan todos los módulos, así que no puede conocer a ninguno")
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("los módulos no forman ciclos entre sí")
	void modulesAreFreeOfCycles() {
		slices()
				.matching(ROOT + "(*)..")
				.should().beFreeOfCycles()
				.check(BOOKHIVE);
	}

	@Test
	@DisplayName("un módulo no usa nada de otro fuera de su infraestructura de salida")
	void modulesMeetOnlyInOutputInfrastructure() {
		for (final String module : MODULES) {
			for (final String other : otherModules(module)) {
				noClasses()
						.that().resideInAPackage(ROOT + module + "..")
						.and().resideOutsideOfPackage(ROOT + module + ".infrastructure.output..")
						.should().dependOnClassesThat().resideInAPackage(ROOT + other + "..")
						.because("ADR-051: los módulos solo se hablan desde la infraestructura de salida")
						.check(BOOKHIVE);
			}
		}
	}

	@Test
	@DisplayName("de otro módulo solo se usan sus puertos de entrada y sus DTO")
	void modulesOnlyUseInputPortsOfOthers() {
		for (final String module : MODULES) {
			for (final String other : otherModules(module)) {
				noClasses()
						.that().resideInAPackage(ROOT + module + "..")
						.should().dependOnClassesThat(resideInAPackage(ROOT + other + "..")
								.and(not(resideInAnyPackage(
										ROOT + other + ".services.input.ports..",
										ROOT + other + ".services.input.dto.."))))
						.because("ADR-051: lo demás de un módulo es privado")
						.check(BOOKHIVE);
			}
		}
	}

	private static String[] otherModules(final String module) {
		return Arrays.stream(MODULES)
				.filter(other -> !other.equals(module))
				.toArray(String[]::new);
	}

	private static String[] modulePackages(final String... extra) {
		return Stream.concat(
				Arrays.stream(MODULES).map(module -> ROOT + module + ".."),
				Arrays.stream(extra))
				.toArray(String[]::new);
	}

}
