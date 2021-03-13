package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.checkstyle.CheckStyleParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;
import io.jenkins.plugins.analysis.core.model.IconLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Provides a parser and customized messages for CheckStyle.
 *
 * @author Ullrich Hafner
 */
public class CheckStyle extends AnalysisModelParser {
    private static final long serialVersionUID = -7944828406964963020L;
    private static final String ID = "checkstyle";

    /** Creates a new instance of {@link CheckStyle}. */
    @DataBoundConstructor
    public CheckStyle() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public IssueParser createParser() {
        return new CheckStyleParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("checkStyle")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
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
            return new IconLabelProvider(ID, Messages.Warnings_CheckStyle_ParserName(), getDescriptionProvider(
            ));
        }
    }
}
