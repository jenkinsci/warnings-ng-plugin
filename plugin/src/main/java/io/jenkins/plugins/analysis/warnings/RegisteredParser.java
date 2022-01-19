package io.jenkins.plugins.analysis.warnings;

import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.registry.ParserDescriptor;
import edu.hm.hafner.analysis.registry.ParserRegistry;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Selects a parser from the registered parsers of the analysis-model library by
 * providing a specific ID.
 *
 * @author Ullrich Hafner
 */
// FIXME: config.jelly needs to be updated
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

        return new StaticAnalysisLabelProvider(descriptor.getId(), getName(), descriptor::getDescription);
    }

    @Override
    public String getActualPattern() {
        return StringUtils.defaultIfBlank(getPattern(), getParserDescriptor().getPattern());
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("analysisParser")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super("analysis-model");
        }
    }
}
