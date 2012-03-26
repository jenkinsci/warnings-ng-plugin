package hudson.plugins.warnings; // NOPMD

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
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
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param group
     *            the parser group this result belongs to
     */
    public WarningsResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result, final String group) {
        super(build, defaultEncoding, result);

        this.group = group;
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
        StringBuilder summary = new StringBuilder();
        int warnings = getNumberOfAnnotations();

        summary.append(ParserRegistry.getParser(group).getLinkName());
        summary.append(": ");
        if (warnings > 0) {
            summary.append(createLink(getUrl()));
        }
        if (warnings == 1) {
            summary.append(Messages.Warnings_ResultAction_OneWarning());
        }
        else {
            summary.append(Messages.Warnings_ResultAction_MultipleWarnings(warnings));
        }
        if (warnings > 0) {
            summary.append("</a>");
        }
        summary.append(".");
        return summary.toString();
    }

    private String createLink(final String url) {
        return String.format("<a href=\"%s\">", url);
    }

    private String getUrl() {
        return WarningsDescriptor.getResultUrl(group);
    }

    @Override
    protected String createDeltaMessage() {
        StringBuilder summary = new StringBuilder();
        if (getNumberOfNewWarnings() > 0) {
            summary.append("<li>");
            summary.append(createLink(getUrl() + "/new"));
            if (getNumberOfNewWarnings() == 1) {
                summary.append(Messages.Warnings_ResultAction_OneNewWarning());
            }
            else {
                summary.append(Messages.Warnings_ResultAction_MultipleNewWarnings(getNumberOfNewWarnings()));
            }
            summary.append("</a></li>");
        }
        if (getNumberOfFixedWarnings() > 0) {
            summary.append("<li>");
            summary.append(createLink(getUrl() + "/fixed"));
            if (getNumberOfFixedWarnings() == 1) {
                summary.append(Messages.Warnings_ResultAction_OneFixedWarning());
            }
            else {
                summary.append(Messages.Warnings_ResultAction_MultipleFixedWarnings(getNumberOfFixedWarnings()));
            }
            summary.append("</a></li>");
        }

        return summary.toString();
    }

    /** {@inheritDoc} */
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

    /**
     * Creates a new instance of {@link WarningsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param history
     *            the history of build results of the associated plug-in
     */
    WarningsResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result, final BuildHistory history) {
        this(build, defaultEncoding, result, history, null);
    }

    /**
     * Creates a new instance of {@link WarningsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param history
     *            the history of build results of the associated plug-in
     */
    WarningsResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result, final BuildHistory history, final String group) {
        super(build, defaultEncoding, result, history);

        this.group = group;
    }

    /**
     * Creates a new instance of {@link WarningsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     */
    WarningsResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final ParserResult result) {
        this(build, defaultEncoding, result, (String)null);
    }
}