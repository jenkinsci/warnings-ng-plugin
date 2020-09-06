package io.jenkins.plugins.analysis.warnings;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import jenkins.model.Jenkins;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Defines several architecture rules for the static analysis utilities.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("hideutilityclassconstructor")
@AnalyzeClasses(packages = "io.jenkins.plugins.analysis.core")
class PluginArchitecture2Test {
    @ArchTest
    static final ArchRule NO_JENKINS_INSTANCE_CALL = noClasses().that().doNotHaveSimpleName("JenkinsFacade")
            .should().callMethod(Jenkins.class, "getInstance")
            .orShould().callMethod(Jenkins.class, "getInstanceOrNull")
            .orShould().callMethod(Jenkins.class, "getActiveInstance")
            .orShould().callMethod(Jenkins.class, "get");

    @ArchTest
    static void printClasses(final JavaClasses classes) {
        assertThat(classes.contain("io.jenkins.plugins.analysis.core.portlets.IssuesChartPortlet")).isTrue();
    }
}
