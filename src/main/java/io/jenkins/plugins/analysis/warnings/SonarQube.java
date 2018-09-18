package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.SonarQubeIssuesParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides parsers and customized messages for SonarQube.
 *
 * @author Ullrich Hafner
 */
public class SonarQube extends StaticAnalysisTool {
    static final String ID = "sonar";

    /** Creates a new instance of {@link SonarQube}. */
    @DataBoundConstructor
    public SonarQube() {
        super();
        // empty constructor required for stapler
    }

    // FIXME: See https://issues.jenkins-ci.org/browse/JENKINS-52463
    // StaticAnalysisTool or Composite 
    @Override
    public IssueParser createParser() {
        return new SonarQubeIssuesParser(); 
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_SonarQube_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }

        @Override
        public String getPattern() {
            return "**/sonar-report.json";
        }
    }
}
