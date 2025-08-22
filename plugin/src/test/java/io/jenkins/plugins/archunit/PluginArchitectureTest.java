package io.jenkins.plugins.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.archunit.ArchitectureRules;

import java.util.Arrays;
import java.util.List;

import io.jenkins.plugins.util.PluginArchitectureRules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Checks several architecture rules for the static analysis utilities.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("hideutilityclassconstructor")
@AnalyzeClasses(packages = "io.jenkins.plugins.analysis")
final class PluginArchitectureTest {
    private PluginArchitectureTest() {
        // prevents instantiation
    }

    @ArchTest
    static final ArchRule NO_EXCEPTIONS_WITH_NO_ARG_CONSTRUCTOR = noClasses()
            .that().haveSimpleNameNotContaining("Benchmark")
            .should().callConstructorWhere(new ExceptionHasNoContext(ParsingCanceledException.class,
                    IncompatibleClassChangeError.class));

    @ArchTest
    static final ArchRule NO_PUBLIC_TEST_CLASSES = ArchitectureRules.NO_PUBLIC_TEST_CLASSES;

    @ArchTest
    static final ArchRule NO_PUBLIC_TEST_METHODS = ArchitectureRules.ONLY_PACKAGE_PRIVATE_TEST_METHODS;

    @ArchTest
    static final ArchRule NO_TEST_API_CALLED = ArchitectureRules.NO_TEST_API_CALLED;

    @ArchTest
    static final ArchRule NO_FORBIDDEN_ANNOTATION_USED = ArchitectureRules.NO_FORBIDDEN_ANNOTATION_USED;

    @ArchTest
    static final ArchRule NO_FORBIDDEN_CLASSES_CALLED = ArchitectureRules.NO_FORBIDDEN_CLASSES_CALLED;

    @ArchTest
    static final ArchRule ONLY_PACKAGE_PRIVATE_ARCHITECTURE_TESTS = ArchitectureRules.ONLY_PACKAGE_PRIVATE_ARCHITECTURE_TESTS;

    @ArchTest
    static final ArchRule NO_JENKINS_INSTANCE_CALL = PluginArchitectureRules.NO_JENKINS_INSTANCE_CALL;

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

    private static class ExceptionHasNoContext extends DescribedPredicate<JavaConstructorCall> {
        private final List<Class<? extends Throwable>> allowedExceptions;

        @SafeVarargs
        ExceptionHasNoContext(final Class<? extends Throwable>... allowedExceptions) {
            super("exception context is missing");

            this.allowedExceptions = Arrays.asList(allowedExceptions);
        }

        @Override
        public boolean test(final JavaConstructorCall javaConstructorCall) {
            var target = javaConstructorCall.getTarget();
            if (!target.getRawParameterTypes().isEmpty()) {
                return false;
            }
            return target.getOwner().isAssignableTo(Throwable.class)
                    && !isPermittedException(target.getOwner());
        }

        private boolean isPermittedException(final JavaClass owner) {
            return allowedExceptions.stream().anyMatch(owner::isAssignableTo);
        }
    }
}
