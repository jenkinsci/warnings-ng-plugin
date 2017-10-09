package io.jenkins.plugins.analysis.core.steps;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.kohsuke.stapler.DataBoundSetter;

import jenkins.model.Jenkins;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.AnnotationParser;
import hudson.plugins.analysis.util.HtmlPrinter;

/**
 * Describes a static analysis tool that reports issues.
 *
 * @author Ullrich Hafner
 */
public abstract class StaticAnalysisTool extends AbstractDescribableImpl<StaticAnalysisTool> implements AnnotationParser, ExtensionPoint {
    private static final String ICONS_PREFIX = "/plugin/analysis-core/icons/";
    private static final String SMALL_ICON_URL = ICONS_PREFIX + "analysis-24x24.png";
    private static final String LARGE_ICON_URL = ICONS_PREFIX + "analysis-48x48.png";

    /**
     * Finds the static analysis tool with the specified ID.
     *
     * @param id
     *         the ID of the tool to find
     *
     * @return the static analysis tool
     * @throws NoSuchElementException
     *         if the tool could not be found
     */
    public static StaticAnalysisTool find(final String id) {
        for (StaticAnalysisTool parser : all()) {
            if (parser.getId().equals(id)) {
                return parser;
            }
        }
        throw new NoSuchElementException("IssueParser not found: " + id);
    }

    private static Collection<? extends StaticAnalysisTool> all() {
        return Jenkins.getInstance().getExtensionList(StaticAnalysisTool.class);
    }

    private final String id;
    private final String defaultPattern;
    private String defaultEncoding;

    @CheckForNull
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Sets the default encoding used to read files (warnings, source code, etc.).
     *
     * @param defaultEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    @DataBoundSetter
    public void setDefaultEncoding(final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    public StaticAnalysisTool(final String id) {
        this(id, "**/*");
    }

    public StaticAnalysisTool(final String id, final String defaultPattern) {
        this.id = id;
        this.defaultPattern = defaultPattern;
    }

    public String getId() {
        return id;
    }

    public String getDefaultPattern() {
        return defaultPattern;
    }

    protected String getName() {
        return "Static Analysis";
    }

    public String getLinkName() {
        return "Static Analysis Issues";
    }

    public String getTrendName() {
        return "Static Analysis Trend";
    }

    public String getSmallIconUrl() {
        return SMALL_ICON_URL;
    }

    public String getLargeIconUrl() {
        return LARGE_ICON_URL;
    }

    public String getResultUrl() {
        return getId() + "Result";
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @param url
     *         the url of the results
     *
     * @return the summary message
     * @since 2.0
     */
    public String getSummary(final int numberOfIssues, final int numberOfModules) {
        return getName() + ": " + new ResultSummaryPrinter().createDefaultSummary(getResultUrl(),
                numberOfIssues, numberOfModules);
    }

    /**
     * Creates a default delta message for the build result.
     *
     * @param newSize
     *         number of new issues
     * @param fixedSize
     *         number of fixed issues
     *
     * @return the summary message
     */
    public String getDeltaMessage(final int newSize, final int fixedSize) {
        HtmlPrinter summary = new HtmlPrinter();
        if (newSize > 0) {
            summary.append(summary.item(
                    summary.link(getResultUrl() + "/new", createNewWarningsLinkName(newSize))));
        }
        if (fixedSize > 0) {
            summary.append(summary.item(
                    summary.link(getResultUrl() + "/fixed", createFixedWarningsLinkName(fixedSize))));
        }
        return summary.toString();
    }

    private static String createNewWarningsLinkName(final int newWarnings) {
        if (newWarnings == 1) {
            return Messages.ResultAction_OneNewWarning();
        }
        else {
            return Messages.ResultAction_MultipleNewWarnings(newWarnings);
        }
    }

    private static String createFixedWarningsLinkName(final int fixedWarnings) {
        if (fixedWarnings == 1) {
            return Messages.ResultAction_OneFixedWarning();
        }
        else {
            return Messages.ResultAction_MultipleFixedWarnings(fixedWarnings);
        }
    }

    public static class StaticAnalysisToolDescriptor extends Descriptor<StaticAnalysisTool> {
        public StaticAnalysisToolDescriptor(final Class<? extends StaticAnalysisTool> clazz) {
            super(clazz);
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return clazz.getSimpleName();
        }
    }
}
