package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

/**
 * Provides a parser and customized messages for FindBugs.
 *
 * @author Ullrich Hafner
 */
public class SpotBugs extends FindBugs {
    private static final long serialVersionUID = -8773197511353021180L;
    static final String ID = "spotbugs";

    /** Creates a new instance of {@link SpotBugs}. */
    @DataBoundConstructor
    public SpotBugs() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends FindBugsDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_SpotBugs_ParserName();
        }

        @Override
        public String getPattern() {
            return "**/target/spotbugsXml.xml";
        }

        @Override
        public String getUrl() {
            return "https://spotbugs.github.io";
        }
    }
}
