package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.PhpParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Php runtime errors and warnings.
 *
 * @author Ullrich Hafner
 */
public class Php extends StaticAnalysisTool {
    private static final long serialVersionUID = 7286546914256953672L;
    static final String ID = "php";

    /** Creates a new instance of {@link Php}. */
    @DataBoundConstructor
    public Php() {
        // empty constructor required for stapler
    }

    @Override
    public PhpParser createParser() {
        return new PhpParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PHP_ParserName();
        }
    }
}
