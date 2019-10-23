package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.ProtoLintParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for ProtoLint.
 *
 * @author David Hart
 */
public class ProtoLint extends ReportScanningTool {

	private static final long serialVersionUID = -5718503998068521571L;
	
	private static final String ID = "protolint";

    /** Creates a new instance of {@link ProtoLint}. */
    @DataBoundConstructor
    public ProtoLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new ProtoLintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("protoLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ProtoLint_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public String getUrl() {
            return "https://github.com/yoheimuta/protolint";
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new IconLabelProvider(getId(), getDisplayName());
        }
    }
}
