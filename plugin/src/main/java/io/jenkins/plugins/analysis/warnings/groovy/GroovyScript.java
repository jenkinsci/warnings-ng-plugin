package io.jenkins.plugins.analysis.warnings.groovy;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.util.Ensure;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.warnings.Messages;

/**
 * Selects a {@link GroovyParser} using the specified ID.
 *
 * @author Ullrich Hafner
 */
public class GroovyScript extends ReportScanningTool {
    private static final long serialVersionUID = 8580859196688994603L;
    private static final String ID = "groovy";

    private final String parserId;

    /**
     * Creates a new instance of {@link GroovyScript}.
     *
     * @param parserId
     *         ID of the Groovy parser
     */
    @DataBoundConstructor
    public GroovyScript(final String parserId) {
        super();

        Ensure.that(ParserConfiguration.getInstance().contains(parserId)).isTrue(
                "There is no Groovy parser defined in the system configuration with ID '%s'", parserId);
        this.parserId = parserId;
    }

    public String getParserId() {
        return parserId;
    }

    @Override
    public IssueParser createParser() {
        return getTool().createParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new StaticAnalysisLabelProvider(parserId, getTool().getName());
    }

    private ReportScanningTool getTool() {
        return ParserConfiguration.getInstance().getParser(parserId);
    }

    @Override
    public String getActualId() {
        return StringUtils.defaultIfBlank(getId(), parserId);
    }

    @Override
    public String getActualName() {
        return StringUtils.defaultIfBlank(getName(), getTool().getActualName());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("groovyScript")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_Groovy_DescribableName();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false; // due to security reasons (JENKINS-54832) running a groovy script on master is prohibited
        }

        /**
         * Returns all registered Groovy parsers. These are packed into a {@link ListBoxModel} in order to show them in
         * the list box of the config.jelly view part.
         *
         * @return the model of the list box
         */
        @SuppressWarnings("unused") // Called from config.jelly
        public ListBoxModel doFillParserIdItems() {
            ListBoxModel options = ParserConfiguration.getInstance().asListBoxModel();
            if (options.isEmpty()) {
                return options.add(Messages.Warnings_Groovy_NoParsersDefined());
            }
            return options;
        }
    }
}
