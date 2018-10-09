package io.jenkins.plugins.analysis.core.testutil;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Defines several architecture rules for the static analysis utilities.
 *
 * @author Ullrich Hafner
 */
// FIXME: forbid calls to Jenkins.getInstance()
class ArchitectureRulesTest extends edu.hm.hafner.ArchitectureRulesTest {
    /**
     * Returns the classes that should be checked.
     *
     * @return the classes that should be checked
     */
    @Override
    protected JavaClasses getAllClasses() {
        return new ClassFileImporter().importPackages("io.jenkins.plugins.analysis");
    }

    /**
     * Test classes should not be public (Junit 5). Only {@link IntegrationTest} classes are required to use JUnit 4.
     */
    @Test
    void shouldNotUsePublicInTestCases() {
        JavaClasses classes = getAllClasses();

        ArchRule noPublicTestClassesDefined = noClasses()
                .that().dontHaveModifier(JavaModifier.ABSTRACT)
                .and().haveNameNotMatching(getClass().getName())
                .and().haveSimpleNameEndingWith("Test")
                .and().haveSimpleNameNotEndingWith("ITest")
                .should().bePublic();

        noPublicTestClassesDefined.check(classes);
    }

    @Override
    protected String[] getForbiddenPackages() {
        return new String[]{"org.apache.commons.lang..",
                "javax.xml.bind..",
                "com.google.common..",
                "hudson.plugins.analysis.."};
    }
}
