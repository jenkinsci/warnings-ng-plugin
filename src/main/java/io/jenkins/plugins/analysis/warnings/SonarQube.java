package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.SonarQubeDiffParser;
import edu.hm.hafner.analysis.parser.SonarQubeIssuesParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;

import hudson.Extension;

/**
 * Provides parsers and customized messages for SonarQube.
 *
 * @author Ullrich Hafner
 */
public class SonarQube extends StaticAnalysisToolSuite {
    static final String ID = "sonar";

    /** Creates a new instance of {@link SonarQube}. */
    @DataBoundConstructor
    public SonarQube() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends AbstractParser> getParsers() {
        return asList(new SonarQubeIssuesParser(), new SonarQubeDiffParser()); 
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("sonarQube")
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
