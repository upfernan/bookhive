package co.com.bookhive.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

// Pruebas de adherencia arquitectónica (ArchUnit).
//
// Revisan que el código respete la arquitectura limpia que pidió el profe.
// Si alguien rompe una regla (por ejemplo, pone una anotación de Spring en el
// dominio), esta prueba falla, el workflow sale en rojo y el cambio no entra.
class ReglasDeArquitecturaTest {

	private static final JavaClasses CLASES = new ClassFileImporter()
			.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
			.importPackages("co.com.bookhive");

	// Tecnologías que no pueden aparecer en el corazón de la aplicación.
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
	void lasCapasRespetanLaReglaDeDependencia() {
		layeredArchitecture()
				.consideringOnlyDependenciesInLayers()
				.layer("Dominio").definedBy("..dominio..")
				.layer("Servicios").definedBy("..servicios..")
				.layer("Infraestructura").definedBy("..infraestructura..")
				.layer("Inicializador").definedBy("..inicializador..")
				.whereLayer("Inicializador").mayNotBeAccessedByAnyLayer()
				.whereLayer("Infraestructura").mayOnlyBeAccessedByLayers("Inicializador")
				.whereLayer("Servicios").mayOnlyBeAccessedByLayers("Infraestructura", "Inicializador")
				.whereLayer("Dominio").mayOnlyBeAccessedByLayers("Servicios", "Infraestructura", "Inicializador")
				.check(CLASES);
	}

	@Test
	@DisplayName("el corazón (dominio, servicios y transversal) no depende de ningún framework")
	void elCorazonNoDependeDeFrameworks() {
		noClasses()
				.that().resideInAnyPackage("..dominio..", "..servicios..", "..transversal..")
				.should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
				.because("el corazón debe quedar lo más limpio posible; Spring solo vive en infraestructura e inicializador")
				.check(CLASES);
	}

	@Test
	@DisplayName("los puertos de entrada y de salida son interfaces")
	void losPuertosSonInterfaces() {
		classes()
				.that().resideInAnyPackage("..servicios.input.ports..", "..servicios.output.ports..")
				.should().beInterfaces()
				.because("toda comunicación entre capas va por interfaces (segregación de interfaces)")
				.check(CLASES);
	}

	@Test
	@DisplayName("los contratos de los casos de uso son interfaces")
	void losContratosDeCasosDeUsoSonInterfaces() {
		classes()
				.that().resideInAPackage("..servicios.casouso..")
				.and().haveSimpleNameEndingWith("CasoUso")
				.should().beInterfaces()
				.check(CLASES);
	}

	@Test
	@DisplayName("las implementaciones de los casos de uso viven en su paquete impl")
	void lasImplementacionesVivenEnImpl() {
		classes()
				.that().haveSimpleNameEndingWith("CasoUsoImpl")
				.should().resideInAPackage("..servicios.casouso..impl..")
				.check(CLASES);
	}

	@Test
	@DisplayName("los controladores solo hablan con los puertos de entrada")
	void losControladoresSoloUsanPuertosDeEntrada() {
		noClasses()
				.that().resideInAPackage("..infraestructura.input..")
				.should().dependOnClassesThat().resideInAnyPackage(
						"..servicios.casouso..",
						"..servicios.input.adapters..",
						"..servicios.output..",
						"..dominio..")
				.because("el controlador nunca llama al caso de uso ni al adaptador: llama al puerto")
				.check(CLASES);
	}

	@Test
	@DisplayName("los casos de uso trabajan con dominio, nunca con DTO")
	void losCasosDeUsoNoConocenDTO() {
		noClasses()
				.that().resideInAPackage("..servicios.casouso..")
				.should().dependOnClassesThat().resideInAPackage("..servicios.input.dto..")
				.because("al caso de uso la información llega como dominio")
				.check(CLASES);
	}

	@Test
	@DisplayName("las entidades solo viven en los repositorios de la infraestructura")
	void lasEntidadesVivenEnInfraestructura() {
		classes()
				.that().areAnnotatedWith("jakarta.persistence.Entity")
				.should().resideInAPackage("..infraestructura.output.repositorios..")
				.because("para nosotros, entidad es lo que se guarda en la base de datos")
				.check(CLASES);
	}

	@Test
	@DisplayName("lo transversal no depende de ningún módulo")
	void loTransversalNoDependeDeLosModulos() {
		noClasses()
				.that().resideInAPackage("..transversal..")
				.should().dependOnClassesThat().resideInAnyPackage("..circulacion..", "..inicializador..")
				.check(CLASES);
	}

}
