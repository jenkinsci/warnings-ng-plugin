package io.jenkins.plugins.analysis.warnings.checkstyle;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.warnings.Messages;

/**
 * Provides a parser and customized messages for CheckStyle.
 *
 * @author Ullrich Hafner
 */
public class CheckStyle extends ReportScanningTool {
    private static final long serialVersionUID = -7944828406964963020L;
    static final String ID = "checkstyle";

    /** Creates a new instance of {@link CheckStyle}. */
    @DataBoundConstructor
    public CheckStyle() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CheckStyleParser createParser() {
        return new CheckStyleParser();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends IconLabelProvider {
        private final CheckStyleRules rules;

        LabelProvider(final CheckStyleRules rules) {
            super(ID, Messages.Warnings_CheckStyle_ParserName());

            this.rules = rules;
        }

        @Override
        public String getDescription(final Issue issue) {
            return rules.getDescription(issue.getType());
        }
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("checkStyle")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        private final CheckStyleRules rules;

        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);

            rules = new CheckStyleRules();
            rules.initialize();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_CheckStyle_ParserName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider(rules);
        }

        @Override
        public String getPattern() {
            return "**/checkstyle-result.xml";
        }

        @Override
        public String getUrl() {
            return "https://checkstyle.org";
        }
    }
}
