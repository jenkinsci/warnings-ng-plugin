package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.registry.ParserDescriptor;
import edu.hm.hafner.analysis.registry.ParserDescriptor.Option;
import edu.hm.hafner.analysis.registry.ParserRegistry;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Describes a static analysis tool from the analysis-model library.
 *
 * @author Ullrich Hafner
 */
public abstract class AnalysisModelParser extends ReportScanningTool {
    private static final long serialVersionUID = 3510579055771471269L;

    @Override
    public IssueParser createParser() {
        return getDescriptor().createParser(configureOptions());
    }

    /**
     * Returns optional options to configure the parser - these options may customize the new parser instance (if
     * supported by the selected).
     *
     * @return the options to use
     */
    protected Option[] configureOptions() {
        return new Option[0];
    }

    @Override
    @SuppressFBWarnings("BC")
    public AnalysisModelParserDescriptor getDescriptor() {
        return (AnalysisModelParserDescriptor) super.getDescriptor();
    }

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
            return new StaticAnalysisLabelProvider(getId(), getDisplayName(), descriptionProvider,
                    analysisModelDescriptor.getType());
        }

        /**
         * Returns a description provider to obtain detailed issue descriptions.
         *
         * @return a description provider
         */
        protected DescriptionProvider getDescriptionProvider() {
            return descriptionProvider;
        }

        /**
         * Returns a new parser to scan a log file and return the issues reported in such a file.
         *
         * @param options
         *         options to configure the parser - may customize the new parser instance (if supported by the selected
         *         tool)
         *
         * @return the parser to use
         */
        public IssueParser createParser(final Option... options) {
            return analysisModelDescriptor.createParser(options);
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

        @NonNull
        @Override
        public final String getDisplayName() {
            return analysisModelDescriptor.getName();
        }
    }

    /**
     * Extracts a description from the associated {@link ParserDescriptor}.
     */
    private static class RegistryIssueDescriptionProvider implements DescriptionProvider {
        private final ParserDescriptor parserDescriptor;

        RegistryIssueDescriptionProvider(final ParserDescriptor parserDescriptor) {
            this.parserDescriptor = parserDescriptor;
        }

        @Override
        public String getDescription(final Issue issue) {
            return parserDescriptor.getDescription(issue);
        }
    }
}
