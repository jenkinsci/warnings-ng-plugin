package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.SonarQubeDiffParser;
import edu.hm.hafner.analysis.parser.SonarQubeIssuesParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides parsers and customized messages for SonarQube.
 *
 * @author Ullrich Hafner
 */
public class SonarQube extends ReportScanningToolSuite {
    private static final long serialVersionUID = 2677209865301252855L;
    
    static final String ID = "sonar";

    /** Creates a new instance of {@link SonarQube}. */
    @DataBoundConstructor
    public SonarQube() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends IssueParser> getParsers() {
        return asList(new SonarQubeIssuesParser(), new SonarQubeDiffParser()); 
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("sonarQube")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
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
            return new IconLabelProvider(getId(), getDisplayName());
        }

        @Override
        public String getPattern() {
            return "**/sonar-report.json";
        }
    }
}
