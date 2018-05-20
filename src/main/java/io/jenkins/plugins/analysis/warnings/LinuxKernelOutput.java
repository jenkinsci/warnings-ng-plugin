package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.LinuxKernelOutputParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the LinuxKernelOutput compiler.
 *
 * @author Johannes Arzt
 */
public class LinuxKernelOutput extends StaticAnalysisTool {
    private static final long serialVersionUID = 6001299329805672199L;
    static final String ID = "linux";

    /** Creates a new instance of {@link LinuxKernelOutput}. */
    @DataBoundConstructor
    public LinuxKernelOutput() {
        // empty constructor required for stapler
    }

    @Override
    public LinuxKernelOutputParser createParser() {
        return new LinuxKernelOutputParser();
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
            return Messages.Warnings_LinuxKernelOutput_ParserName();
        }
    }
}
