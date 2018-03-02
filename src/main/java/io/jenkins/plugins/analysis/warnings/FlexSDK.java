package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.FlexSdkParser;

/**
 * Provides a parser and customized messages for FLEX SDK.
 *
 * @author Ullrich Hafner
 */
public class FlexSDK extends StaticAnalysisTool {
    static final String ID = "flex";

    /** Creates a new instance of {@link FlexSDK}. */
    @DataBoundConstructor
    public FlexSDK() {
        // empty constructor required for stapler
    }

    @Override
    public FlexSdkParser createParser() {
        return new FlexSdkParser();
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
            return Messages.Warnings_Flex_ParserName();
        }
    }
}
