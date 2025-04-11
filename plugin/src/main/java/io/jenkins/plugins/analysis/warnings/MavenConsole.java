package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for the Maven console output.
 *
 * @author Aykut Yilmaz
 */
public class MavenConsole extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 4642573591598798109L;
    private static final String ID = "maven-warnings";

    /** Creates a new instance of {@link MavenConsole}. */
    @DataBoundConstructor
    public MavenConsole() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("mavenConsole")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean isPostProcessingEnabled() {
            return false;
        }
    }
}
