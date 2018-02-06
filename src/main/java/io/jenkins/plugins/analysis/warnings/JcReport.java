package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.jcreport.JcReportParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the JcReport compiler.
 *
 * @author Johannes Arzt
 */
public class JcReport extends StaticAnalysisTool {
    static final String ID = "jc-report";

    /** Creates a new instance of {@link JcReport}. */
    @DataBoundConstructor
    public JcReport() {
        // empty constructor required for stapler
    }

    @Override
    public JcReportParser createParser() {
        return new JcReportParser();
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
            return Messages.Warnings_JCReport_ParserName();
        }
    }
}
