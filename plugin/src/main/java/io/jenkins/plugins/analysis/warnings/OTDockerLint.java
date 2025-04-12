package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for {@code ot-docker-lint} json report.
 *
 * @author Abhishek Dubey
 */
public class OTDockerLint extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 42L;
    private static final String ID = "ot-docker-linter";

    /** Creates a new instance of {@link OTDockerLint}. */
    @DataBoundConstructor
    public OTDockerLint() {
        super();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("otDockerLint")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), getDescriptionProvider());
        }
    }
}
