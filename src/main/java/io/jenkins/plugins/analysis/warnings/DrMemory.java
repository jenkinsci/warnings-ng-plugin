package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.DrMemoryParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Dr. Memory Errors.
 *
 * @author Ullrich Hafner
 */
public class DrMemory extends StaticAnalysisTool {
    static final String ID = "dr-memory";

    /** Creates a new instance of {@link DrMemory}. */
    @DataBoundConstructor
    public DrMemory() {
        // empty constructor required for stapler
    }

    @Override
    public DrMemoryParser createParser() {
        return new DrMemoryParser();
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
            return Messages.Warnings_DrMemory_ParserName();
        }
    }
}
