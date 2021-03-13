package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.registry.ParserDescriptor;
import edu.hm.hafner.analysis.registry.ParserRegistry;

/**
 * Describes a static analysis tool from the analysis-model library.
 *
 * @author Ullrich Hafner
 */
public abstract class AnalysisModelParser extends ReportScanningTool {
    private static final long serialVersionUID = 3510579055771471269L;

    /** Descriptor for {@link AnalysisModelParser}. **/
    public abstract static class AnalysisModelParserDescriptor extends ReportScanningToolDescriptor {
        private static final ParserRegistry REGISTRY = new ParserRegistry();

        private final RegistryIssueDescriptionProvider descriptionProvider;
        private final ParserDescriptor analysisModelDescriptor;

        /**
         * Creates a new instance of {@link AnalysisModelParserDescriptor} with the given ID.
         *
         * @param id
         *         the unique ID of the tool
         */
        protected AnalysisModelParserDescriptor(final String id) {
            this(id, id);
        }

        /**
         * Creates a new instance of {@link AnalysisModelParserDescriptor} with the given ID.
         *
         * @param id
         *         the unique ID of the tool
         * @param descriptionId
         *         the description ID of the tool in the analysis model module
         */
        protected AnalysisModelParserDescriptor(final String id, final String descriptionId) {
            super(id);

            analysisModelDescriptor = REGISTRY.get(descriptionId);
            descriptionProvider = new RegistryIssueDescriptionProvider(analysisModelDescriptor);
        }

        /**
         * Returns a {@link StaticAnalysisLabelProvider} that will render all tool specific labels.
         *
         * @return a tool specific {@link StaticAnalysisLabelProvider}
         */
        @Override
        public StaticAnalysisLabelProvider getLabelProvider() {
            return new StaticAnalysisLabelProvider(getId(), getDisplayName(), descriptionProvider);
        }

        /**
         * Returns a description provider to obtain detailed issue descriptions.
         *
         * @return a description provider
         */
        protected RegistryIssueDescriptionProvider getDescriptionProvider() {
            return descriptionProvider;
        }

        @Override
        public String getPattern() {
            return analysisModelDescriptor.getPattern();
        }

        @Override
        public String getHelp() {
            return analysisModelDescriptor.getHelp();
        }

        @Override
        public String getUrl() {
            return analysisModelDescriptor.getUrl();
        }
    }

    /**
     * Extracts a description from the associated {@link ParserDescriptor}.
     */
    private static class RegistryIssueDescriptionProvider implements DescriptionProvider {
        private final ParserDescriptor parserDescriptor;

        RegistryIssueDescriptionProvider(final ParserDescriptor parserDescriptor) {
            if (parserDescriptor == null) {
                throw new NullPointerException();
            }
            this.parserDescriptor = parserDescriptor;
        }

        @Override
        public String getDescription(final Issue issue) {
            return parserDescriptor.getDescription(issue);
        }
    }
}
