package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.SbtScalacParser;
import edu.hm.hafner.analysis.parser.ScalacParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningToolSuite;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Scala compiler.
 *
 * @author Ullrich Hafner
 */
public class Scala extends ReportScanningToolSuite {
    private static final long serialVersionUID = -3425343204163661812L;
    static final String ID = "scala";

    /** Creates a new instance of {@link Scala}. */
    @DataBoundConstructor
    public Scala() {
        super();
        // empty constructor required for stapler
    }

    @Override
    protected Collection<? extends IssueParser> getParsers() {
        return asList(new ScalacParser(), new SbtScalacParser());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("scala")
    @Extension
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ScalaParser_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }
}
