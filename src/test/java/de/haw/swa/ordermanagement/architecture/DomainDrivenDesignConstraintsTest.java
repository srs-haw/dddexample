package de.haw.swa.ordermanagement.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Comprehensive ArchUnit test suite for Domain Driven Design architectural constraints.
 * 
 * This test class validates key DDD principles:
 * - Domain layer independence
 * - Proper layer separation
 * - Repository pattern implementation
 * - Aggregate root design
 * - Domain event patterns
 * - Naming conventions
 * 
 * Tests are designed to catch common DDD violations and ensure architectural integrity.
 */
@DisplayName("Domain Driven Design Constraints")
class DomainDrivenDesignConstraintsTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("de.haw.swa.ordermanagement");
    }

    // ============ Core DDD Layer Rules ============

    @Test
    @DisplayName("Domain layer should not depend on application layer")
    void domainLayerShouldNotDependOnApplicationLayer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..application..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on interfaces layer")
    void domainLayerShouldNotDependOnInterfacesLayer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..interfaces..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on configuration")
    void domainLayerShouldNotDependOnConfiguration() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..config..");

        rule.check(importedClasses);
    }

    // ============ Clean Architecture Rules ============

    @Test
    @DisplayName("Layered architecture boundaries should be respected")
    void layeredArchitectureBoundariesShouldBeRespected() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Interface").definedBy("..interfaces..")
                .layer("Application").definedBy("..application..")
                .layer("Domain").definedBy("..domain..")
                .layer("Config").definedBy("..config..")
                
                .whereLayer("Interface").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Interface", "Config")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Interface", "Config")
                .whereLayer("Config").mayNotBeAccessedByAnyLayer();

        rule.check(importedClasses);
    }

    // ============ Repository Pattern Rules ============

    @Test
    @DisplayName("Repository interfaces should be in domain layer")
    void repositoryInterfacesShouldBeInDomainLayer() {
        ArchRule rule = classes()
                .that().areInterfaces()
                .and().haveSimpleNameEndingWith("Repository")
                .should().resideInAPackage("..domain.repository..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Repository interfaces should only be accessed by application services")
    void repositoryInterfacesShouldOnlyBeAccessedByApplicationServices() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain.repository..")
                .should().onlyBeAccessed()
                .byClassesThat()
                .resideInAnyPackage(
                        "..application.service..",
                        "..interfaces.rest..",
                        "..config.."
                );

        rule.check(importedClasses);
    }

    // ============ Component Placement Rules ============

    @Test
    @DisplayName("Controllers should be in interfaces layer")
    void controllersShouldBeInInterfacesLayer() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().resideInAPackage("..interfaces.rest..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Services should be in application layer")
    void servicesShouldBeInApplicationLayer() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.stereotype.Service")
                .should().resideInAPackage("..application.service..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain events should be in domain layer")
    void domainEventsShouldBeInDomainLayer() {
        ArchRule rule = classes()
                .that().implement("de.haw.swa.ordermanagement.domain.model.shared.DomainEvent")
                .should().resideInAPackage("..domain.model..events..");

        rule.check(importedClasses);
    }

    // ============ Aggregate Design Rules ============

    @Test
    @DisplayName("Main domain entities should extend AggregateRoot")
    void mainDomainEntitiesShouldExtendAggregateRoot() {
        ArchRule rule = classes()
                .that().haveSimpleName("Order")
                .or().haveSimpleName("Product")
                .or().haveSimpleName("Customer")
                .should().beAssignableTo("de.haw.swa.ordermanagement.domain.model.shared.AggregateRoot");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Aggregates should be self-contained")
    void aggregatesShouldBeSelfContained() {
        ArchRule orderAggregateRule = classes()
                .that().resideInAPackage("..domain.model.order..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..domain.model.order..",
                        "..domain.model.shared..",
                        "java..",
                        "jakarta.persistence.."
                );

        ArchRule productAggregateRule = classes()
                .that().resideInAPackage("..domain.model.product..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..domain.model.product..",
                        "..domain.model.shared..",
                        "java..",
                        "jakarta.persistence.."
                );

        ArchRule customerAggregateRule = classes()
                .that().resideInAPackage("..domain.model.customer..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..domain.model.customer..",
                        "..domain.model.shared..",
                        "java..",
                        "jakarta.persistence.."
                );

        orderAggregateRule.check(importedClasses);
        productAggregateRule.check(importedClasses);
        customerAggregateRule.check(importedClasses);
    }

    // ============ Service and Transaction Rules ============

    @Test
    @DisplayName("Domain coordinating services should depend on domain objects")
    void domainCoordinatingServicesShouldDependOnDomainObjects() {
        ArchRule rule = classes()
                .that().resideInAPackage("..application.service..")
                .and().haveSimpleNameEndingWith("Service")
                .and().haveSimpleNameNotContaining("Payment")
                .and().haveSimpleNameNotContaining("Shipping")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..domain.model..", "..domain.repository..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Only application services should be transactional")
    void onlyApplicationServicesShouldBeTransactional() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("org.springframework.transaction.annotation.Transactional")
                .should().resideInAPackage("..application.service..")
                .orShould().resideInAPackage("..config..");

        rule.check(importedClasses);
    }

    // ============ Naming Convention Rules ============

    @Test
    @DisplayName("DDD naming conventions should be followed")
    void dddNamingConventionsShouldBeFollowed() {
        ArchRule controllerRule = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().haveSimpleNameEndingWith("Controller");

        ArchRule serviceRule = classes()
                .that().areAnnotatedWith("org.springframework.stereotype.Service")
                .should().haveSimpleNameEndingWith("Service");

        ArchRule repositoryRule = classes()
                .that().areInterfaces()
                .and().resideInAPackage("..domain.repository..")
                .should().haveSimpleNameEndingWith("Repository");

        ArchRule configRule = classes()
                .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
                .should().haveSimpleNameEndingWith("Config")
                .orShould().haveSimpleNameEndingWith("Configuration");

        controllerRule.check(importedClasses);
        serviceRule.check(importedClasses);
        repositoryRule.check(importedClasses);
        configRule.check(importedClasses);
    }

    // ============ Anti-Corruption Layer Rules ============

    @Test
    @DisplayName("DTOs should be in interfaces layer")
    void dtosShouldBeInInterfacesLayer() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .should().resideInAPackage("..interfaces.rest.dto..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Configuration should be separated from business logic")
    void configurationShouldBeSeparatedFromBusinessLogic() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("..config..");

        rule.check(importedClasses);
    }

    // ============ Domain Event Rules ============

    @Test
    @DisplayName("Domain events should implement DomainEvent interface")
    void domainEventsShouldImplementDomainEventInterface() {
        ArchRule rule = classes()
                .that().resideInAPackage("..domain.model..events..")
                .should().implement("de.haw.swa.ordermanagement.domain.model.shared.DomainEvent");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain events should have immutable fields")
    void domainEventsShouldHaveImmutableFields() {
        ArchRule rule = classes()
                .that().implement("de.haw.swa.ordermanagement.domain.model.shared.DomainEvent")
                .should().haveOnlyFinalFields();

        rule.check(importedClasses);
    }
}