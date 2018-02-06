package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.QACSourceCodeAnalyserParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the PRQA QA-C Sourcecode Analyser.
 *
 * @author Ullrich Hafner
 */
public class QACSourceCodeAnalyser extends StaticAnalysisTool {
    static final String ID = "qac";

    /** Creates a new instance of {@link QACSourceCodeAnalyser}. */
    @DataBoundConstructor
    public QACSourceCodeAnalyser() {
        // empty constructor required for stapler
    }

    @Override
    public QACSourceCodeAnalyserParser createParser() {
        return new QACSourceCodeAnalyserParser();
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
            return Messages.Warnings_QAC_ParserName();
        }
    }
}
