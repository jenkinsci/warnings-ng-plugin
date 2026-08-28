package io.jenkins.plugins.analysis.warnings.groovy;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.util.Ensure;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.Serial;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.warnings.Messages;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Selects a {@link GroovyParser} using the specified ID.
 *
 * @author Ullrich Hafner
 */
public class GroovyScript extends ReportScanningTool {
    @Serial
    private static final long serialVersionUID = 8580859196688994603L;
    private static final String ID = "groovy";

    private final String parserId;
    @CheckForNull
    private GroovyParser parser;

    /**
     * Creates a new instance of {@link GroovyScript}.
     *
     * @param parserId
     *         ID of the Groovy parser
     */
    @DataBoundConstructor
    public GroovyScript(final String parserId) {
        super();

        this.parserId = StringUtils.defaultString(parserId);
    }

    public String getParserId() {
        return parserId;
    }

    @CheckForNull
    public GroovyParser getParser() {
        return parser;
    }

    /**
     * Sets a parser that is defined as part of the current job configuration.
     *
     * @param parser
     *         local parser definition
     */
    @DataBoundSetter
    public void setParser(final GroovyParser parser) {
        this.parser = parser;
    }

    @Override
    public IssueParser createParser() {
        return getTool().createParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        var configuredParser = getTool();
        return new StaticAnalysisLabelProvider(configuredParser.getId(), configuredParser.getName());
    }

    private GroovyParser getTool() {
        if (parser != null) {
            return parser;
        }

        Ensure.that(StringUtils.isNotBlank(parserId)).isTrue(
                "No Groovy parser defined. Configure either 'parserId' or 'parser'");
        Ensure.that(ParserConfiguration.getInstance().contains(parserId)).isTrue(
                "There is no Groovy parser defined in the system configuration with ID '%s'", parserId);

        return ParserConfiguration.getInstance().getParser(parserId);
    }

    @Override
    public String getActualId() {
        return StringUtils.defaultIfBlank(getId(), getTool().getId());
    }

    @Override
    public String getActualName() {
        return StringUtils.defaultIfBlank(getName(), getTool().getName());
    }

    @Override
    protected boolean canScanConsoleLog() {
        return parser == null && getDescriptor().canScanConsoleLog();
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
            // Maintenance note: This was previously hard-coded as false.
            // If the code is enhanced to run the groovy code within a sandboxed environment
            // then it can be hard-coded as true and the configuration option removed.
            // Until then, we let the Jenkins admin decide.
            // See JENKINS-54832 for ongoing discussion.
            return ParserConfiguration.getInstance().isConsoleLogScanningPermitted();
        }

        /**
         * Returns all registered Groovy parsers. These are packed into a {@link ListBoxModel} in order to show them in
         * the list box of the config.jelly view part.
         *
         * @param project
         *         the project that is configured
         *
         * @return the model of the list box
         */
        @SuppressWarnings("unused") // Called from config.jelly
        @POST
        public ListBoxModel doFillParserIdItems(@AncestorInPath final BuildableItem project) {
            if (new JenkinsFacade().hasPermission(Item.CONFIGURE, project)) {
                var options = ParserConfiguration.getInstance().asListBoxModel();
                if (options.isEmpty()) {
                    return options.add(Messages.Warnings_Groovy_NoParsersDefined());
                }
                return options;
            }
            return new ListBoxModel();
        }
    }
}
