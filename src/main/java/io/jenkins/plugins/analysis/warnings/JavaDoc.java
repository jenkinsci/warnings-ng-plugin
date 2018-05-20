package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.JavaDocParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.warnings.Java.JavaLabelProvider;

import hudson.Extension;

/**
 * Provides customized messages for the JavaDoc parser.
 *
 * @author Ullrich Hafner
 */
public class JavaDoc extends StaticAnalysisTool {
    private static final long serialVersionUID = -3987566418736570996L;
    static final String ID = "javadoc";

    /** Creates a new instance of {@link JavaDoc}. */
    @DataBoundConstructor
    public JavaDoc() {
        // empty constructor required for stapler
    }

    @Override
    public JavaDocParser createParser() {
        return new JavaDocParser();
    }

    /** Provides the labels for the static analysis tool. */
    public static class JavaDocLabelProvider extends JavaLabelProvider {
        public JavaDocLabelProvider() {
            super(ID, Messages.Warnings_JavaDoc_ParserName());
        }
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
            return Messages.Warnings_JavaDoc_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new JavaDocLabelProvider();
        }
    }
}