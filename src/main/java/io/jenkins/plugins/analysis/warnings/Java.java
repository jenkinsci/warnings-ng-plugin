package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.JavacParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Java compiler.
 *
 * @author Ullrich Hafner
 */
public class Java extends StaticAnalysisTool {
    private static final long serialVersionUID = 2254154391638811877L;
    static final String ID = "java";

    /** Creates a new instance of {@link NagFortran}. */
    @DataBoundConstructor
    public Java() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public JavacParser createParser() {
        return new JavacParser();
    }
    
    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_JavaParser_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }
}
