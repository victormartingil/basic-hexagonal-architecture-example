package com.example.hexarch.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * ARCHITECTURE TESTS - ArchUnit
 *
 * Tests que validan las reglas de la Arquitectura Hexagonal.
 * Estos tests FALLARÁN si alguien viola las reglas de dependencias.
 *
 * OBJETIVO:
 * - Garantizar que Domain no depende de Application o Infrastructure
 * - Garantizar que Application no depende de Infrastructure
 * - Validar la separación de capas
 * - Detectar violaciones automáticamente en CI/CD
 *
 * REGLAS DE HEXAGONAL ARCHITECTURE:
 * ✅ Infrastructure → Application → Domain
 * ❌ Domain NO puede depender de Application o Infrastructure
 * ❌ Application NO puede depender de Infrastructure
 */
@DisplayName("🏛️ Hexagonal Architecture Tests")
class HexagonalArchitectureTest {

    // Clases a analizar (todo el código de producción)
    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        // Importar todas las clases del proyecto (excluyendo tests)
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.hexarch");
    }

    /**
     * NESTED CLASS: Domain Layer Rules
     *
     * Valida que el Domain Layer esté completamente aislado
     */
    @Nested
    @DisplayName("🏰 Domain Layer - Debe estar aislado")
    class DomainLayerRules {

        @Test
        @DisplayName("Domain NO debe depender de Application")
        void domainShouldNotDependOnApplication() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .because("Domain debe estar libre de dependencias de Application (Regla de Hexagonal Architecture)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Domain NO debe depender de Infrastructure")
        void domainShouldNotDependOnInfrastructure() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .because("Domain debe estar libre de dependencias de Infrastructure (Regla de Hexagonal Architecture)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Domain NO debe usar anotaciones de Spring")
        void domainShouldNotUseSpringAnnotations() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .because("Domain debe ser Java puro, sin dependencias de frameworks");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Domain NO debe usar anotaciones de JPA")
        void domainShouldNotUseJpaAnnotations() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .because("Domain no debe conocer detalles de persistencia");

            rule.check(importedClasses);
        }
    }

    /**
     * NESTED CLASS: Application Layer Rules
     *
     * Valida que el Application Layer no dependa de Infrastructure
     */
    @Nested
    @DisplayName("🔄 Application Layer - Debe ser independiente de Infrastructure")
    class ApplicationLayerRules {

        @Test
        @DisplayName("Application NO debe depender de Infrastructure")
        void applicationShouldNotDependOnInfrastructure() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .because("Application debe ser independiente de detalles de implementación (Dependency Inversion)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("User Services deben implementar Use Cases")
        void userServicesShouldImplementUseCases() {
            ArchRule rule = classes()
                .that().resideInAPackage("..user.application.service..")
                .and().haveSimpleNameEndingWith("Service")
                .should().implement(com.example.hexarch.user.application.port.input.CreateUserUseCase.class)
                .orShould().implement(com.example.hexarch.user.application.port.input.GetUserUseCase.class)
                .because("User Services deben implementar interfaces de Use Cases (Input Ports). " +
                         "Nota: Servicios técnicos (EmailService, NotificationService) no son Use Cases, " +
                         "son Output Adapters y no necesitan implementar esta regla.");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Application Services deben estar anotados con @Service")
        void servicesShouldBeAnnotatedWithService() {
            ArchRule rule = classes()
                .that().resideInAPackage("..application.service..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith(org.springframework.stereotype.Service.class)
                .because("Services deben ser componentes de Spring para inyección de dependencias");

            rule.check(importedClasses);
        }
    }

    /**
     * NESTED CLASS: Infrastructure Layer Rules
     *
     * Valida las convenciones de Infrastructure
     */
    @Nested
    @DisplayName("🔌 Infrastructure Layer - Adaptadores")
    class InfrastructureLayerRules {

        @Test
        @DisplayName("Controllers deben estar en el paquete input.rest (o subdirectorios)")
        void controllersShouldBeInInputRestPackage() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("..infrastructure.adapter.input.rest..")
                .because("Controllers son Input Adapters y deben estar en el paquete correcto (permite subdirectorios)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Controllers deben estar anotados con @RestController")
        void controllersShouldBeAnnotatedWithRestController() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .because("Controllers deben ser REST Controllers de Spring");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Repository Adapters deben implementar interfaces de Output Ports")
        void repositoryAdaptersShouldImplementRepositoryInterfaces() {
            ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.adapter.output.persistence..")
                .and().haveSimpleNameEndingWith("RepositoryAdapter")
                .should().implement(com.example.hexarch.user.application.port.output.UserRepository.class)
                .because("Repository Adapters deben implementar interfaces de Output Ports");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("JPA Entities deben estar en el paquete persistence")
        void entitiesShouldBeInPersistencePackage() {
            ArchRule rule = classes()
                .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("..infrastructure.adapter.output.persistence..")
                .because("JPA Entities son detalles de implementación y pertenecen a Infrastructure");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("JPA Entities deben tener sufijo 'Entity'")
        void entitiesShouldHaveEntitySuffix() {
            ArchRule rule = classes()
                .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().haveSimpleNameEndingWith("Entity")
                .because("JPA Entities deben tener sufijo 'Entity' para distinguirse del Domain");

            rule.check(importedClasses);
        }
    }

    /**
     * NESTED CLASS: Layered Architecture Rules
     *
     * Valida la arquitectura en capas completa
     */
    @Nested
    @DisplayName("🏗️ Layered Architecture - Flujo de dependencias")
    class LayeredArchitectureRules {

        @Test
        @DisplayName("Arquitectura en capas: Infrastructure → Application → Domain")
        void layeredArchitectureShouldBeRespected() {
            ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()

                // Definir capas
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Infrastructure").definedBy("..infrastructure..")

                // Reglas de acceso
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")

                .because("La arquitectura hexagonal requiere Infrastructure → Application → Domain");

            rule.check(importedClasses);
        }
    }

    /**
     * NESTED CLASS: Naming Conventions
     *
     * Valida las convenciones de nombres
     */
    @Nested
    @DisplayName("📝 Naming Conventions - Nomenclatura DDD + Hexagonal")
    class NamingConventionsRules {

        @Test
        @DisplayName("Commands y Queries deben tener sufijo apropiado")
        void commandsAndQueriesShouldHaveAppropiateSuffix() {
            ArchRule rule = classes()
                .that().resideInAPackage("..application.port.input..")
                .and().areNotInterfaces()
                .and().areRecords()
                .and().haveSimpleNameNotEndingWith("Result")  // Results son DTOs de salida, no de entrada
                .should().haveSimpleNameEndingWith("Command")
                .orShould().haveSimpleNameEndingWith("Query")
                .because("DTOs de entrada (no Results) deben seguir nomenclatura CQRS (Command/Query)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Use Cases deben tener sufijo 'UseCase'")
        void useCasesShouldHaveUseCaseSuffix() {
            ArchRule rule = classes()
                .that().resideInAPackage("..application.port.input..")
                .and().areInterfaces()
                .should().haveSimpleNameEndingWith("UseCase")
                .because("Interfaces de casos de uso deben tener sufijo 'UseCase'");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Value Objects deben estar en el paquete valueobject")
        void valueObjectsShouldBeInValueObjectPackage() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain.model.valueobject..")
                .should().beRecords()
                .orShould().haveSimpleNameEndingWith("Email")
                .orShould().haveSimpleNameEndingWith("Username")
                .because("Value Objects idealmente deben ser records inmutables (o clases finales con igualdad por valor)");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Domain Exceptions deben heredar de DomainException")
        void domainExceptionsShouldExtendDomainException() {
            ArchRule rule = classes()
                .that().resideInAPackage("..domain.exception..")
                .and().areNotInterfaces()
                .and().haveSimpleNameEndingWith("Exception")
                .should().beAssignableTo(com.example.hexarch.user.domain.exception.DomainException.class)
                .because("Excepciones de dominio deben heredar de DomainException");

            rule.check(importedClasses);
        }
    }

    /**
     * NESTED CLASS: Package Structure
     *
     * Valida la estructura de paquetes
     */
    @Nested
    @DisplayName("📦 Package Structure - Organización por módulos")
    class PackageStructureRules {

        @Test
        @DisplayName("Clases de domain deben estar en paquete ..domain..")
        void domainClassesShouldBeInDomainPackage() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Exception")
                .and().resideInAPackage("..user..")
                .and().resideOutsideOfPackages("..infrastructure..", "..application..")
                .should().resideInAPackage("..domain.exception..")
                .because("Excepciones de dominio deben estar en el paquete domain.exception");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Value Objects deben residir en el paquete correcto")
        void valueObjectsShouldBeInCorrectPackage() {
            ArchRule rule = classes()
                .that().areRecords()
                .and().resideInAPackage("..domain..")
                .should().resideInAnyPackage("..domain.model.valueobject..", "..domain.event..")
                .because("Records de domain deben ser Value Objects o Events");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Repository interfaces (Output Ports) deben estar en application.port.output")
        void repositoryInterfacesShouldBeInOutputPort() {
            ArchRule rule = classes()
                .that().areInterfaces()
                .and().haveSimpleNameEndingWith("Repository")
                .and().resideOutsideOfPackage("..infrastructure..")  // Excluye Spring Data repositories
                .should().resideInAPackage("..application.port.output..")
                .because("Repository interfaces (Output Ports) deben estar en application.port.output");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("Mappers deben estar en infrastructure.adapter")
        void mappersShouldBeInInfrastructure() {
            ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Mapper")
                .should().resideInAPackage("..infrastructure.adapter..")
                .because("Mappers son detalles de implementación y pertenecen a Infrastructure");

            rule.check(importedClasses);
        }
    }
}
