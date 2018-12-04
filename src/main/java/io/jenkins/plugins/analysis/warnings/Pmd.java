package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.parser.pmd.PmdParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PMD.
 *
 * @author Ullrich Hafner
 */
public class Pmd extends ReportScanningTool {
    private static final long serialVersionUID = -7600332469176914690L;
    static final String ID = "pmd";

    /** Creates a new instance of {@link Pmd}. */
    @DataBoundConstructor
    public Pmd() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public boolean canScanConsoleLog() {
        return false;
    }

    @Override
    public PmdParser createParser() {
        return new PmdParser();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends IconLabelProvider {
        private final PmdMessages messages;

        LabelProvider(final PmdMessages messages) {
            super(ID, Messages.Warnings_PMD_ParserName());

            this.messages = messages;
        }

        @Override
        public String getDescription(final Issue issue) {
            return messages.getMessage(issue.getCategory(), issue.getType());
        }
    }

    /** Descriptor for this static analysis tool. */
    @Symbol({"pmd", "pmdParser"})
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        private final PmdMessages messages;

        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);

            messages = new PmdMessages();
            messages.initialize();
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PMD_ParserName();
        }

        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new LabelProvider(messages);
        }

        @Override
        public String getPattern() {
            return "**/pmd.xml";
        }

        @Override
        public String getUrl() {
            return "https://pmd.github.io";
        }
    }
}

