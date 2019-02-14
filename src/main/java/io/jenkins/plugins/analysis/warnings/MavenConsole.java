package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.MavenConsoleParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Maven console output.
 *
 * @author Aykut Yilmaz
 */
public class MavenConsole extends ReportScanningTool {
    private static final long serialVersionUID = 4642573591598798109L;
    static final String ID = "maven-warnings";

    /** Creates a new instance of {@link MavenConsole}. */
    @DataBoundConstructor
    public MavenConsole() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public MavenConsoleParser createParser() {
        return new MavenConsoleParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("mavenConsole")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Maven_ParserName();
        }

        @Override
        protected boolean isConsoleLog() {
            return true;
        }

        @Override
        public boolean isPostProcessingEnabled() {
            return false;
        }
    }
}
