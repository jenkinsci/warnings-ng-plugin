package io.jenkins.plugins.analysis.warnings;

import java.util.Arrays;
import javax.xml.parsers.SAXParser;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.XMLReader;

import com.google.common.annotations.VisibleForTesting;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

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
@AnalyzeClasses(packages = "io.jenkins.plugins.analysis..")
class PluginArchitectureTest {
    /** Digester must not be used directly, rather use a SecureDigester instance. */
    @ArchTest
    static final ArchRule NO_DIGESTER_CONSTRUCTOR_CALLED =
            noClasses().that().doNotHaveSimpleName("SecureDigester")
                    .should().callConstructor(Digester.class)
                    .orShould().callConstructor(Digester.class, SAXParser.class)
                    .orShould().callConstructor(Digester.class, XMLReader.class)
                    .orShould().callMethod(DigesterLoader.class, "newDigester");

    /** Test classes should not be public (Junit 5). */
    @ArchTest
    static final ArchRule NO_PUBLIC_TEST_CLASSES =
            noClasses().that().doNotHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameEndingWith("Test")
                    .and(doNot(have(simpleNameEndingWith("ITest"))))
                    .should().bePublic();

    /** Test classes should not use Junit 4. */
    // TODO: see https://github.com/TNG/ArchUnit/issues/136
    @ArchTest
    static final ArchRule NO_JUNIT_4 =
            noClasses().that(doNot(have(simpleNameEndingWith("ITest"))))
                    .should().dependOnClassesThat().resideInAnyPackage("org.junit");

    /**
     * Methods or constructors that are annotated with {@link VisibleForTesting} must not be called by other classes.
     * These methods are meant to be {@code private}. Only test classes are allowed to call these methods.
     */
    @ArchTest
    static final ArchRule NO_TEST_API_CALLED =
            noClasses().that().haveSimpleNameNotEndingWith("Test")
                    .should().callCodeUnitWhere(new AccessRestrictedToTests());

    /** Prevents that classes use visible but forbidden API. */
    @ArchTest
    static final ArchRule NO_FORBIDDEN_PACKAGE_ACCESSED =
            noClasses().should().accessClassesThat().resideInAnyPackage(
                    "org.apache.commons.lang..", "javax.xml.bind..",
                    "net.jcip.annotations..", "javax.annotation..", "junit..", "org.hamcrest..");

    /** Prevents that classes use visible but forbidden API. */
    @ArchTest
    static final ArchRule NO_FORBIDDEN_CLASSES_CALLED
            = noClasses().should().callCodeUnitWhere(new TargetIsForbiddenClass(
            "org.junit.jupiter.api.Assertions", "org.junit.Assert"));

    /**
     * Direct calls to {@link Jenkins#getInstance()} are prohibited since this method requires a running Jenkins
     * instance. Otherwise the accessor of this method cannot be unit tested. Create a new {@link JenkinsFacade} object
     * to access the running Jenkins instance. If your required method is missing you need to add it to {@link
     * JenkinsFacade}.
     */
    @ArchTest
    static final ArchRule NO_JENKINS_INSTANCE_CALL =
            noClasses().that().doNotHaveSimpleName("JenkinsFacade")
                    .should().callMethod(Jenkins.class, "getInstance");

    /**
     * Matches if a call from outside the defining class uses a method or constructor annotated with {@link
     * VisibleForTesting}. There are two exceptions:
     * <ul>
     * <li>The method is called on the same class</li>
     * <li>The method is called in a method also annotated with {@link VisibleForTesting}</li>
     * </ul>
     */
    private static class AccessRestrictedToTests extends DescribedPredicate<JavaCall<?>> {
        AccessRestrictedToTests() {
            super("access is restricted to tests");
        }

        @Override
        public boolean apply(final JavaCall<?> input) {
            return isVisibleForTesting(input.getTarget())
                    && !input.getOriginOwner().equals(input.getTargetOwner())
                    && !isVisibleForTesting(input.getOrigin());
        }

        private boolean isVisibleForTesting(final CanBeAnnotated target) {
            return target.isAnnotatedWith(VisibleForTesting.class)
                    || target.isAnnotatedWith(edu.hm.hafner.util.VisibleForTesting.class);
        }
    }

    /**
     * Matches if a code unit of one of the registered classes has been called.
     */
    private static class TargetIsForbiddenClass extends DescribedPredicate<JavaCall<?>> {
        private final String[] classes;

        TargetIsForbiddenClass(final String... classes) {
            super("forbidden class");

            this.classes = Arrays.copyOf(classes, classes.length);
        }

        @Override
        public boolean apply(final JavaCall<?> input) {
            return StringUtils.containsAny(input.getTargetOwner().getFullName(), classes);
        }
    }
}
