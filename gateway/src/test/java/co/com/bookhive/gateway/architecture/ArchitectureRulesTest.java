package co.com.bookhive.gateway.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class ArchitectureRulesTest {

	private static final JavaClasses GATEWAY = new ClassFileImporter()
			.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
			.importPackages("co.com.bookhive.gateway");

	private static final String[] FRAMEWORKS = {
			"org.springframework..",
			"reactor..",
			"io.lettuce..",
			"io.netty..",
			"com.nimbusds..",
			"tools.jackson..",
			"com.fasterxml.." };

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
				.whereLayer("Services").mayOnlyBeAccessedByLayers("Infrastructure", "Initializer")
				.whereLayer("Domain").mayOnlyBeAccessedByLayers("Services", "Initializer")
				.check(GATEWAY);
	}

	@Test
	@DisplayName("el dominio, los servicios y el crosscutting no dependen de ningún framework ni librería reactiva")
	void coreDoesNotDependOnFrameworks() {
		noClasses()
				.that().resideInAnyPackage("..domain..", "..services..", "..crosscutting..")
				.should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
				.because("Spring, Reactor, Redis y Nimbus solo viven en infrastructure e initializer")
				.check(GATEWAY);
	}

	@Test
	@DisplayName("los puertos de entrada y de salida son interfaces")
	void portsAreInterfaces() {
		classes()
				.that().resideInAnyPackage("..services.input.ports", "..services.output.ports")
				.should().beInterfaces()
				.check(GATEWAY);
	}

	@Test
	@DisplayName("los contratos de los casos de uso son interfaces y sus implementaciones viven en impl")
	void useCasesFollowTheContractLayout() {
		classes()
				.that().resideInAPackage("..services.usecase..")
				.and().resideOutsideOfPackage("..impl..")
				.should().beInterfaces()
				.check(GATEWAY);

		classes()
				.that().haveSimpleNameEndingWith("UseCaseImpl")
				.should().resideInAPackage("..services.usecase..impl..")
				.check(GATEWAY);
	}

	@Test
	@DisplayName("los filtros de entrada solo hablan con los puertos de entrada, nunca con el caso de uso ni el dominio")
	void filtersOnlyUseInputPorts() {
		noClasses()
				.that().resideInAPackage("..infrastructure.input..")
				.should().dependOnClassesThat().resideInAnyPackage(
						"..services.usecase..",
						"..services.input.adapters..",
						"..domain..",
						"..infrastructure.output.attempts..",
						"..infrastructure.output.audit..")
				.because("el filtro llama al puerto de entrada, como el controlador en el backend")
				.check(GATEWAY);
	}

	@Test
	@DisplayName("el crosscutting no depende de ninguna capa")
	void crosscuttingIsIndependent() {
		noClasses()
				.that().resideInAPackage("..crosscutting..")
				.should().dependOnClassesThat().resideInAnyPackage(
						"..domain..", "..services..", "..infrastructure..", "..initializer..")
				.check(GATEWAY);
	}

}
