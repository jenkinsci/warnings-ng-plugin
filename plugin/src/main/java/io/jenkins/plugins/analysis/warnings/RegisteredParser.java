package io.jenkins.plugins.analysis.warnings;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.registry.ParserDescriptor;
import edu.hm.hafner.analysis.registry.ParserRegistry;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Selects a parser from the registered parsers of the analysis-model library by
 * providing a specific ID.
 *
 * @author Ullrich Hafner
 */
public class RegisteredParser extends ReportScanningTool {
    private static final long serialVersionUID = 22286587552212078L;

    private static final ParserRegistry REGISTRY = new ParserRegistry();

    private final String analysisModelId;

    /**
     * Creates a new instance of {@link RegisteredParser}.
     *
     * @param analysisModelId
     *         the unique ID of the tool in the analysis-model module
     */
    @DataBoundConstructor
    public RegisteredParser(final String analysisModelId) {
        super();

        this.analysisModelId = analysisModelId;
        if (!REGISTRY.contains(analysisModelId)) {
            throw new NoSuchElementException("No such parser found with the specified ID: " + analysisModelId);
        }
    }

    public String getAnalysisModelId() {
        return analysisModelId;
    }

    private ParserDescriptor getParserDescriptor() {
        return REGISTRY.get(analysisModelId);
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), getParserDescriptor().getId());
    }

    @Override
    public String getName() {
        return StringUtils.defaultIfBlank(super.getName(), getParserDescriptor().getName());
    }

    @Override
    public IssueParser createParser() {
        return getParserDescriptor().createParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        ParserDescriptor descriptor = getParserDescriptor();

        return new StaticAnalysisLabelProvider(descriptor.getId(), getName(), descriptor::getDescription,
                descriptor.getType());
    }

    @Override
    public String getActualPattern() {
        return StringUtils.defaultIfBlank(getPattern(), getParserDescriptor().getPattern());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("analysisParser")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        @VisibleForTesting
        static final String ANALYSIS_MODEL_ID = "analysis-model";
        private static final ListBoxModel EMPTY_MODEL = new ListBoxModel();

        private final ListBoxModel availableParsers;
        private final JenkinsFacade jenkinsFacade;

        /** Creates the descriptor instance. */
        public Descriptor() {
            this(new JenkinsFacade());
        }

        @VisibleForTesting
        Descriptor(final JenkinsFacade jenkinsFacade) {
            super(ANALYSIS_MODEL_ID);

            availableParsers = new ListBoxModel();
            List<ParserDescriptor> allDescriptors = new ParserRegistry().getAllDescriptors();
            allDescriptors.sort(Comparator.comparing(ParserDescriptor::getName));
            allDescriptors.stream().map(d -> new Option(d.getName(), d.getId())).forEach(availableParsers::add);
            this.jenkinsFacade = jenkinsFacade;
        }

        /**
         * Returns a model with all available parsers.
         *
         * @return a model with all available parsers
         */
        @POST
        public ListBoxModel doFillAnalysisModelIdItems() {
            if (jenkinsFacade.hasPermission(Jenkins.READ)) {
                return availableParsers;
            }
            return EMPTY_MODEL;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.RegisteredParser_Name();
        }
    }
}
