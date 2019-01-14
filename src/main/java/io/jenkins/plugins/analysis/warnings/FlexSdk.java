package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.FlexSdkParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for FLEX SDK.
 *
 * @author Ullrich Hafner
 */
public class FlexSdk extends ReportScanningTool {
    private static final long serialVersionUID = 8786339674737448596L;
    static final String ID = "flex";

    /** Creates a new instance of {@link FlexSdk}. */
    @DataBoundConstructor
    public FlexSdk() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public FlexSdkParser createParser() {
        return new FlexSdkParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("flexSdk")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Flex_ParserName();
        }
    }
}
