package io.jenkins.plugins.archunit;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.net.URL;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.Configuration.*;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.*;

/**
 * Checks the package architecture of this plugin.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("hideutilityclassconstructor")
@AnalyzeClasses(packages = "io.jenkins.plugins.analysis", importOptions = DoNotIncludeTests.class)
final class PackageArchitectureTest {
    private static final URL PACKAGE_DESIGN = PackageArchitectureTest.class.getResource("/design.puml");

    @ArchTest
    static final ArchRule ADHERES_TO_PACKAGE_DESIGN
            = classes().should(adhereToPlantUmlDiagram(PACKAGE_DESIGN,
            consideringOnlyDependenciesInAnyPackage("io.jenkins.plugins.analysis..")));

    private PackageArchitectureTest() {
    }
}
