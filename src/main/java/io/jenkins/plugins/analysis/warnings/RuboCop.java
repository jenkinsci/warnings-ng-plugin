package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.RuboCopParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides parsers and customized messages for RuboCop.
 *
 * @author Ullrich Hafner
 */
public class RuboCop extends ReportScanningTool {
    private static final long serialVersionUID = -6972204105563729273L;
    
    static final String ID = "rubocop";

    /** Creates a new instance of {@link RuboCop}. */
    @DataBoundConstructor
    public RuboCop() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new RuboCopParser(); 
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("ruboCop")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_RuboCop_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(ID, Messages.Warnings_RuboCop_ParserName());
        }
    }
}
