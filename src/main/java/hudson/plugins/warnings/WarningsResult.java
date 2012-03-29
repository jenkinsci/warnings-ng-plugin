package hudson.plugins.warnings; // NOPMD

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.HtmlPrinter;
import hudson.plugins.warnings.parser.ParserRegistry;
import hudson.plugins.warnings.parser.Warning;

import com.thoughtworks.xstream.XStream;

/**
 * Represents the results of the warning analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class WarningsResult extends BuildResult {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -137460587767210579L;
    /** The group of the parser. @since 4.0 */
    private final String group;

    /**
     * Creates a new instance of {@link WarningsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param history
     *            the build history
     * @param result
     *            the parsed result with all annotations
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param group
     *            the parser group this result belongs to
     */
    public WarningsResult(final AbstractBuild<?, ?> build, final BuildHistory history,
            final ParserResult result, final String defaultEncoding, final String group) {
        this(build, history, result, defaultEncoding, group, true);
    }

    WarningsResult(final AbstractBuild<?, ?> build, final BuildHistory history,
            final ParserResult result, final String defaultEncoding, final String group,
            final boolean canSerialize) {
        super(build, history, result, defaultEncoding);

        this.group = group;
        if (canSerialize) {
            serializeAnnotations(result.getAnnotations());
        }
    }

    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("warning", Warning.class);
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getSummary() {
        HtmlPrinter summary = new HtmlPrinter();
        summary.append(ParserRegistry.getParser(group).getLinkName());
        summary.append(": ");

        int warnings = getNumberOfAnnotations();
        if (warnings > 0) {
            summary.append(summary.link(getUrl(), getSummaryText(warnings)));
        }
        else {
            summary.append(getSummaryText(warnings));
        }
        summary.append(".");
        return summary.toString();
    }

    private String getSummaryText(final int warnings) {
        if (warnings == 1) {
            return Messages.Warnings_ResultAction_OneWarning();
        }
        else {
            return Messages.Warnings_ResultAction_MultipleWarnings(warnings);
        }
    }

    private String getUrl() {
        return WarningsDescriptor.getResultUrl(group);
    }

    @Override
    protected String createDeltaMessage() {
        HtmlPrinter summary = new HtmlPrinter();
        if (getNumberOfNewWarnings() > 0) {
            summary.append(summary.item(
                    summary.link(getUrl() + "/new", createNewText())));
        }
        if (getNumberOfFixedWarnings() > 0) {
            summary.append(summary.item(
                    summary.link(getUrl() + "/fixed", createFixedText())));
        }

        return summary.toString();
    }

    private String createFixedText() {
        if (getNumberOfFixedWarnings() == 1) {
            return Messages.Warnings_ResultAction_OneFixedWarning();
        }
        else {
            return Messages.Warnings_ResultAction_MultipleFixedWarnings(getNumberOfFixedWarnings());
        }
    }

    private String createNewText() {
        if (getNumberOfNewWarnings() == 1) {
            return Messages.Warnings_ResultAction_OneNewWarning();
        }
        else {
            return Messages.Warnings_ResultAction_MultipleNewWarnings(getNumberOfNewWarnings());
        }
    }

    @Override
    protected String getSerializationFileName() {
        if (group == null) { // prior 4.0
            return "compiler-warnings.xml";
        }
        else {
            return "compiler-" + ParserRegistry.getUrl(group) + "-warnings.xml";
        }
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        if (group == null) {
            return Messages.Warnings_ProjectAction_Name();
        }
        else {
            return ParserRegistry.getParser(group).getLinkName().toString();
        }
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return WarningsResultAction.class;
    }
}