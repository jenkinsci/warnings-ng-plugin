package io.jenkins.plugins.analysis.warnings;

import java.io.Serializable;
import javax.xml.parsers.SAXParser;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.xml.sax.XMLReader;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import edu.hm.hafner.util.ArchitectureRules;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import hudson.Extension;

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
@AnalyzeClasses(packages = "io.jenkins.plugins.analysis..")
class PluginArchitectureTest {
    @ArchTest
    static final ArchRule NO_JENKINS_INSTANCE_CALL = PluginArchitectureRules.NO_JENKINS_INSTANCE_CALL;

    @ArchTest
    static final ArchRule NO_PUBLIC_TEST_CLASSES = PluginArchitectureRules.NO_PUBLIC_TEST_CLASSES;

    @ArchTest
    static final ArchRule NO_TEST_API_CALLED = ArchitectureRules.NO_TEST_API_CALLED;

    @ArchTest
    static final ArchRule NO_FORBIDDEN_PACKAGE_ACCESSED = PluginArchitectureRules.NO_FORBIDDEN_PACKAGE_ACCESSED;

    @ArchTest
    static final ArchRule NO_FORBIDDEN_CLASSES_CALLED = ArchitectureRules.NO_FORBIDDEN_CLASSES_CALLED;

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

    @ArchTest
    static final ArchRule JAVASCRIPTMETHOD_PUBLIC_CLASS =
            methods().that().areAnnotatedWith(JavaScriptMethod.class)
                    .should().beDeclaredInClassesThat().arePublic();

    @ArchTest
    static final ArchRule EXTENSION_PUBLIC_CLASS =
            classes().that().areAnnotatedWith(Extension.class)
                    .should().bePublic();

    @ArchTest
    static final ArchRule DATABOUND_PUBLIC_CLASS_AND_CONSTRUCTOR =
            constructors().that().areAnnotatedWith(DataBoundConstructor.class)
                    .should().bePublic()
                    .andShould().beDeclaredInClassesThat().arePublic();

    @ArchTest
    static final ArchRule JAVASCRIPTMETHOD_AND_DATABOUND_PUBLIC_METHOD =
            methods().that().areAnnotatedWith(JavaScriptMethod.class)
                    .or().areAnnotatedWith(DataBoundSetter.class)
                    .should().bePublic();

    @ArchTest
    static final ArchRule READRESOLVE_SERIALIZABLE_CLASS =
            methods().that().haveFullName("readResolve").and().haveRawReturnType(Object.class)
                    .should().beDeclaredInClassesThat().implement(Serializable.class)
                    .andShould().beProtected();
}
