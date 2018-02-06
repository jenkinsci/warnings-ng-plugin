package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.FlexSDKParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

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
    public FlexSDKParser createParser() {
        return new FlexSDKParser();
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
