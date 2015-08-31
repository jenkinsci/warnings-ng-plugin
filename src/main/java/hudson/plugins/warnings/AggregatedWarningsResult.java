package hudson.plugins.warnings;

import com.thoughtworks.xstream.XStream;

import hudson.model.Run;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.warnings.parser.Warning;

/**
 * Represents the aggregated results of all warnings parsers. One instance of this class is persisted for each build via
 * an XML file.
 *
 * @author Marvin Schütz
 * @author Sebastian Hansbauer
 */
public class AggregatedWarningsResult extends BuildResult {
    private static final long serialVersionUID = 4572019928324067680L;

    /**
     * Creates a new instance of {@link AggregatedWarningsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param history
     *            build history
     * @param result
     *            the parsed result with all annotations
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public AggregatedWarningsResult(final Run<?, ?> build, final BuildHistory history, final ParserResult result,
                                    final String defaultEncoding) {
        super(build, history, result, defaultEncoding);

        serializeAnnotations(result.getAnnotations());
    }

    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("warning", Warning.class);
    }

    @Override
    public String getHeader() {
        return Messages.Warnings_Totals_Name();
    }

    @Override
    public String getSummary() {
        return Messages.Warnings_ProjectAction_Name() + ": " + createDefaultSummary(getUrl(), getNumberOfAnnotations(), getNumberOfModules());
    }

    private static String getUrl() {
        return WarningsDescriptor.RESULT_URL;
    }

    @Override
    protected String createDeltaMessage() {
        return createDefaultDeltaMessage(getUrl(), getNumberOfNewWarnings(), getNumberOfFixedWarnings());
    }

    @Override
    protected String getSerializationFileName() {
        return "aggregated-warnings.xml";
    }

    @Override
    public String getDisplayName() {
        return Messages.Warnings_ProjectAction_Name();
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return AggregatedWarningsResultAction.class;
    }
}
