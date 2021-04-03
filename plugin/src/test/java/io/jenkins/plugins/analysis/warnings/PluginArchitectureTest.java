package io.jenkins.plugins.analysis.warnings;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import edu.hm.hafner.util.ArchitectureRules;

import io.jenkins.plugins.util.PluginArchitectureRules;

import static com.tngtech.archunit.base.DescribedPredicate.*;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Defines several architecture rules for the static analysis utilities.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("hideutilityclassconstructor")
@AnalyzeClasses(packages = "io.jenkins.plugins.analysis")
class PluginArchitectureTest {
    @ArchTest
    static final ArchRule NO_TEST_API_CALLED = ArchitectureRules.NO_TEST_API_CALLED;

    @ArchTest
    static final ArchRule NO_FORBIDDEN_ANNOTATION_USED = ArchitectureRules.NO_FORBIDDEN_ANNOTATION_USED;

    @ArchTest
    static final ArchRule NO_FORBIDDEN_CLASSES_CALLED = ArchitectureRules.NO_FORBIDDEN_CLASSES_CALLED;

    @ArchTest
    static final ArchRule NO_JENKINS_INSTANCE_CALL = PluginArchitectureRules.NO_JENKINS_INSTANCE_CALL;

    @ArchTest
    static final ArchRule NO_PUBLIC_TEST_CLASSES = PluginArchitectureRules.NO_PUBLIC_TEST_CLASSES;

    @ArchTest
    static final ArchRule NO_PUBLIC_TEST_METHODS = PluginArchitectureRules.NO_PUBLIC_TEST_METHODS;

    @ArchTest
    static final ArchRule NO_PUBLIC_ARCHITECTURE_TESTS = PluginArchitectureRules.NO_PUBLIC_ARCHITECTURE_TESTS;

    @ArchTest
    static final ArchRule NO_FORBIDDEN_PACKAGE_ACCESSED = PluginArchitectureRules.NO_FORBIDDEN_PACKAGE_ACCESSED;

    @ArchTest
    static final ArchRule AJAX_PROXY_METHOD_MUST_BE_IN_PUBLIC_CLASS = PluginArchitectureRules.AJAX_PROXY_METHOD_MUST_BE_IN_PUBLIC_CLASS;

    @ArchTest
    static final ArchRule DATA_BOUND_CONSTRUCTOR_MUST_BE_IN_PUBLIC_CLASS = PluginArchitectureRules.DATA_BOUND_CONSTRUCTOR_MUST_BE_IN_PUBLIC_CLASS;

    @ArchTest
    static final ArchRule DATA_BOUND_SETTER_MUST_BE_IN_PUBLIC_CLASS = PluginArchitectureRules.DATA_BOUND_SETTER_MUST_BE_IN_PUBLIC_CLASS;

    @ArchTest
    static final ArchRule USE_POST_FOR_VALIDATION_END_POINTS = PluginArchitectureRules.USE_POST_FOR_VALIDATION_END_POINTS;

    @ArchTest
    static final ArchRule USE_POST_FOR_LIST_MODELS_RULE = PluginArchitectureRules.USE_POST_FOR_LIST_AND_COMBOBOX_FILL;

    /** Test classes should not use Junit 4. */
    // TODO: see https://github.com/TNG/ArchUnit/issues/136
    @ArchTest
    static final ArchRule NO_JUNIT_4 =
            noClasses().that(doNot(
                    have(simpleNameEndingWith("ITest"))
                            .or(have(simpleNameStartingWith("Integration")))
                            .or(have(simpleName("ToolsLister")))))
                    .should().dependOnClassesThat().resideInAnyPackage("org.junit");
}
