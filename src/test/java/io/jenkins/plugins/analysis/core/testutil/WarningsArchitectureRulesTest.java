package io.jenkins.plugins.analysis.core.testutil;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.*;

/**
 * Defines several architecture rules for the static analysis utilities.
 *
 * @author Ullrich Hafner
 */
// TODO: forbid calls to Jenkins.getInstance()
class WarningsArchitectureRulesTest extends edu.hm.hafner.ArchitectureRulesTest {
    @Override
    protected JavaClasses getAllClasses() {
        return new ClassFileImporter().importPackages("io.jenkins.plugins.analysis");
    }

    @Override
    protected DescribedPredicate<? super JavaClass> areAllowedPublicTestClasses() {
        return have(simpleNameEndingWith("ITest"));
    }

    @Override
    protected String[] getForbiddenPackages() {
        return new String[]{"org.apache.commons.lang..",
                "javax.xml.bind..",
                "com.google.common..",
                "hudson.plugins.analysis.."};
    }
}
