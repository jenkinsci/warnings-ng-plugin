package io.jenkins.plugins.analysis.warnings;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides parsers and customized messages for SonarQube.
 *
 * @author Ullrich Hafner
 */
public class SonarQube extends AnalysisModelParser {
    private static final long serialVersionUID = 2677209865301252855L;

    private static final String ID = "sonar";

    /** Creates a new instance of {@link SonarQube}. */
    @DataBoundConstructor
    public SonarQube() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("sonarQube")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_SonarQube_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName(), getDescriptionProvider());
        }
    }
}
