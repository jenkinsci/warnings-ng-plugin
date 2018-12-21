package io.jenkins.plugins.analysis.warnings;

import javax.xml.parsers.SAXParser;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.xml.sax.XMLReader;

import com.google.common.annotations.VisibleForTesting;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.*;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Defines several architecture rules for the static analysis utilities.
 *
 * @author Ullrich Hafner
 */
// TODO: forbid calls to Jenkins.getInstance()
@SuppressWarnings("hideutilityclassconstructor")
@AnalyzeClasses(packages = "io.jenkins.plugins.analysis..")
class PluginArchitectureTest {
    /** Digester must not be used directly, rather use a SecureDigester instance. */
    @ArchTest
    static final ArchRule NO_DIGESTER_CONSTRUCTOR_CALLED =
            noClasses()
                    .that().dontHaveSimpleName("SecureDigester")
                    .should().callConstructor(Digester.class)
                    .orShould().callConstructor(Digester.class, SAXParser.class)
                    .orShould().callConstructor(Digester.class, XMLReader.class)
                    .orShould().callMethod(DigesterLoader.class, "newDigester");

    /** Test classes should not be public (Junit 5). */
    @ArchTest
    static final ArchRule NO_PUBLIC_TEST_CLASSES =
            noClasses()
                    .that().dontHaveModifier(JavaModifier.ABSTRACT)
                    .and().haveSimpleNameEndingWith("Test")
                    .and(dont(have(simpleNameEndingWith("ITest"))))
                    .should().bePublic();

    /**
     * Methods or constructors that are annotated with {@link VisibleForTesting} must not be called by other classes.
     * These methods are meant to be {@code private}. Only test classes are allowed to call these methods.
     */
    @ArchTest
    static final ArchRule NO_TEST_API_CALLED =
            noClasses()
                    .that().haveSimpleNameNotEndingWith("Test")
                    .should().callCodeUnitWhere(new AccessRestrictedToTests());

    /** Prevents that classes use visible but forbidden API. */
    @ArchTest
    static final ArchRule NO_RESTRICTED_API_CALLED
            = noClasses()
            .should().accessClassesThat().resideInAnyPackage(
                    "org.apache.commons.lang..", "javax.xml.bind..");
    // TODO: .orShould().accessClassesThat().haveFullyQualifiedName(File.class.getName());

    @ArchTest
    static final ArchRule CONFORMS_TO_PACKAGE_DESIGN =
            classes().that().resideInAPackage("..charts").should().onlyBeAccessed()
                    .byAnyPackage("..charts", "..core.model");

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
            return input.getTarget().isAnnotatedWith(VisibleForTesting.class)
                    && !input.getOriginOwner().equals(input.getTargetOwner())
                    && !input.getOrigin().isAnnotatedWith(VisibleForTesting.class);
        }
    }
}
