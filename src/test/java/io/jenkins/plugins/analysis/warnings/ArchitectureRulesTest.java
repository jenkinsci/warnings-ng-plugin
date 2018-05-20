package io.jenkins.plugins.analysis.warnings;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

/**
 * Defines several architecture rules for the static analysis utilities.
 *
 * @author Ullrich Hafner
 */
class ArchitectureRulesTest extends io.jenkins.plugins.analysis.core.testutil.ArchitectureRulesTest {
    @Override
    protected JavaClasses getAllClasses() {
        return new ClassFileImporter().importPackages("io.jenkins.plugins.analysis.warnings");
    }
}
