package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.BuckminsterParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Buckminster Compiler.
 *
 * @author Ullrich Hafner
 */
public class Buckminster extends StaticAnalysisTool {
    private static final long serialVersionUID = 7067423260823622207L;
    static final String ID = "buckminster";

    /** Creates a new instance of {@link Buckminster}. */
    @DataBoundConstructor
    public Buckminster() {
        // empty constructor required for stapler
    }

    @Override
    public BuckminsterParser createParser() {
        return new BuckminsterParser();
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
            return Messages.Warnings_Buckminster_ParserName();
        }
    }
}
