package io.jenkins.plugins.analysis.warnings;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.parsers.SAXParser;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.xml.sax.XMLReader;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import edu.hm.hafner.util.ArchitectureRules;

import org.kohsuke.stapler.verb.POST;
import hudson.model.Descriptor;
import hudson.util.ComboBoxModel;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.util.JenkinsFacade;
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
    static final ArchRule NO_FORBIDDEN_PACKAGE_ACCESSED = PluginArchitectureRules.NO_FORBIDDEN_PACKAGE_ACCESSED;

    @ArchTest
    static final ArchRule AJAX_PROXY_METHOD_MUST_BE_IN_PUBLIC_CLASS = PluginArchitectureRules.AJAX_PROXY_METHOD_MUST_BE_IN_PUBLIC_CLASS;

    @ArchTest
    static final ArchRule DATA_BOUND_CONSTRUCTOR_MUST_BE_IN_PUBLIC_CLASS = PluginArchitectureRules.DATA_BOUND_CONSTRUCTOR_MUST_BE_IN_PUBLIC_CLASS;

    @ArchTest
    static final ArchRule DATA_BOUND_SETTER_MUST_BE_IN_PUBLIC_CLASS = PluginArchitectureRules.DATA_BOUND_SETTER_MUST_BE_IN_PUBLIC_CLASS;

    @ArchTest
    static final ArchRule USE_POST_FOR_VALIDATION_END_POINTS = PluginArchitectureRules.USE_POST_FOR_VALIDATION_END_POINTS;

    /**
     * List model methods that are used as AJAX end points must use @POST and have a permission check.
     */
    public static final ArchRule USE_POST_FOR_LIST_MODELS =
            methods().that().areDeclaredInClassesThat().areAssignableTo(Descriptor.class)
                    .and().haveNameMatching("doFill[A-Z].*")
                    .and().haveRawReturnType(new AllowedClasses(ComboBoxModel.class, ListBoxModel.class))
                    .should().beAnnotatedWith(POST.class)
                    .andShould().bePublic()
                    .andShould(new HavePermissionCheck());

    @ArchTest
    static final ArchRule USE_POST_FOR_LIST_MODELS_RULE = USE_POST_FOR_LIST_MODELS;


    /** Digester must not be used directly, rather use a SecureDigester instance. */
    @ArchTest
    static final ArchRule NO_DIGESTER_CONSTRUCTOR_CALLED =
            noClasses().that().doNotHaveSimpleName("SecureDigester")
                    .should().callConstructor(Digester.class)
                    .orShould().callConstructor(Digester.class, SAXParser.class)
                    .orShould().callConstructor(Digester.class, XMLReader.class)
                    .orShould().callMethod(DigesterLoader.class, "newDigester");

    /** Test classes should not use Junit 4. */
    // TODO: see https://github.com/TNG/ArchUnit/issues/136
    @ArchTest
    static final ArchRule NO_JUNIT_4 =
            noClasses().that(doNot(
                    have(simpleNameEndingWith("ITest"))
                            .or(have(simpleNameStartingWith("Integration")))
                            .or(have(simpleName("ToolsLister")))))
                    .should().dependOnClassesThat().resideInAnyPackage("org.junit");

    private static class HavePermissionCheck extends ArchCondition<JavaMethod> {
        HavePermissionCheck() {
            super("should have a permission check");
        }

        @Override
        public void check(final JavaMethod item, final ConditionEvents events) {
            Set<JavaCall<?>> callsFromSelf = item.getCallsFromSelf();

            if (callsFromSelf.stream().anyMatch(
                    javaCall -> javaCall.getTarget().getOwner().getFullName().equals(JenkinsFacade.class.getName())
                    && javaCall.getTarget().getName().equals("hasPermission"))) {
                return;
            }
            events.add(SimpleConditionEvent.violated(item,
                    String.format("JenkinsFacade not called in %s in %s",
                            item.getDescription(), item.getSourceCodeLocation())));
        }
    }

    private static class AllowedClasses extends DescribedPredicate<JavaClass> {
        private final List<String> allowedClasses;

        AllowedClasses(final Class<?>... classes) {
            super("raw return type of any of %s", Arrays.toString(classes));

            allowedClasses = Arrays.stream(classes).map(Class::getName).collect(Collectors.toList());
        }

        @Override
        public boolean apply(final JavaClass input) {
            return allowedClasses.contains(input.getFullName());
        }
    }
}
