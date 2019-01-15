package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.NoSuchElementException;
import edu.hm.hafner.util.VisibleForTesting;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;

/**
 * Creates detail objects for the selected link in the issues detail view. Each link might be visualized by a
 * specialized view.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("ParameterNumber")
public class DetailFactory {
    private static final Report EMPTY = new Report();
    private static final String LINK_SEPARATOR = ".";

    private final JenkinsFacade jenkins;

    /**
     * Creates a new instance of {@link DetailFactory}.
     */
    public DetailFactory() {
        this(new JenkinsFacade());
    }

    @VisibleForTesting
    DetailFactory(final JenkinsFacade jenkinsFacade) {
        this.jenkins = jenkinsFacade;
    }

    /**
     * Returns a detail object for the selected element for the specified issues.
     *
     * @param link
     *         the link to identify the sub page to show
     * @param owner
     *         the build as owner of the detail page
     * @param result
     *         the overall analysis result
     * @param allIssues
     *         the issues to get the details for
     * @param newIssues
     *         the new issues to get the details for
     * @param fixedIssues
     *         the fixed issues to get the details for
     * @param outstandingIssues
     *         the outstanding issues to get the details for
     * @param sourceEncoding
     *         the encoding to use when displaying source files
     * @param parent
     *         the parent of the selected object
     *
     * @return the dynamic result of this module detail view
     */
    @SuppressWarnings({"npathcomplexity", "PMD.CyclomaticComplexity"})
    Object createTrendDetails(final String link, final Run<?, ?> owner, final AnalysisResult result,
            final Report allIssues, final Report newIssues,
            final Report outstandingIssues, final Report fixedIssues,
            final Charset sourceEncoding, final IssuesDetail parent) {
        StaticAnalysisLabelProvider labelProvider = parent.getLabelProvider();

        if (link.contains(LINK_SEPARATOR)) {
            return createFilteredView(link, owner,
                    result, allIssues, newIssues, outstandingIssues, fixedIssues,
                    sourceEncoding, parent, labelProvider);
        }
        else {
            return createNewDetailView(link, owner,
                    result, allIssues, newIssues, outstandingIssues, fixedIssues,
                    sourceEncoding, parent, labelProvider);
        }
    }

    private Object createFilteredView(final String link, final Run<?, ?> owner, final AnalysisResult result,
            final Report allIssues, final Report newIssues, final Report outstandingIssues, final Report fixedIssues,
            final Charset sourceEncoding, final IssuesDetail parent, final StaticAnalysisLabelProvider labelProvider) {
        String plainLink = removePropertyPrefix(link);
        if (link.startsWith("source.")) {
            Issue issue = allIssues.findById(UUID.fromString(plainLink));
            if (ConsoleLogHandler.isInConsoleLog(issue.getFileName())) {
                try (Stream<String> consoleLog = jenkins.readConsoleLog(owner)) {
                    return new ConsoleDetail(owner, consoleLog, issue.getLineStart(), issue.getLineEnd());
                }
            }
            else {
                String description = labelProvider.getSourceCodeDescription(owner, issue);
                String icon = jenkins.getImagePath(labelProvider.getSmallIconUrl());
                try (Reader affectedFile = jenkins.readBuildFile(owner, issue.getFileName(), sourceEncoding)) {
                    return new SourceDetail(owner, affectedFile, issue, description, icon);
                }
                catch (IOException e) {
                    StringReader fallback = new StringReader(
                            String.format("%s%n%s", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e)));
                    return new SourceDetail(owner, fallback, issue, description, icon);
                }
            }
        }

        String url = parent.getUrl() + "/" + plainLink;
        String property = StringUtils.substringBefore(link, ".");
        Predicate<Issue> filter = createPropertyFilter(plainLink, property);
        Report selectedIssues = allIssues.filter(filter);
        return new IssuesDetail(owner, result,
                selectedIssues, newIssues.filter(filter), outstandingIssues.filter(filter),
                fixedIssues.filter(filter), getDisplayNameOfDetails(property, selectedIssues), url,
                labelProvider, sourceEncoding);
    } 

    private Object createNewDetailView(final String link, final Run<?, ?> owner, final AnalysisResult result,
            final Report allIssues, final Report newIssues, final Report outstandingIssues, final Report fixedIssues,
            final Charset sourceEncoding, final IssuesDetail parent, final StaticAnalysisLabelProvider labelProvider) {
        String url = parent.getUrl() + "/" + link;

        if ("all".equalsIgnoreCase(link)) {
            return new IssuesDetail(owner, result, allIssues, newIssues, outstandingIssues, fixedIssues,
                    labelProvider.getLinkName(), url, labelProvider, sourceEncoding);
        }
        if ("fixed".equalsIgnoreCase(link)) {
            return new FixedWarningsDetail(owner, result, fixedIssues, url, labelProvider, sourceEncoding);
        }
        if ("new".equalsIgnoreCase(link)) {
            return new IssuesDetail(owner, result, newIssues, newIssues, EMPTY,
                    EMPTY, Messages.New_Warnings_Header(), url, labelProvider, sourceEncoding);
        }
        if ("outstanding".equalsIgnoreCase(link)) {
            return new IssuesDetail(owner, result, outstandingIssues, EMPTY, outstandingIssues,
                    EMPTY, Messages.Outstanding_Warnings_Header(), url, labelProvider, sourceEncoding);
        }
        if ("info".equalsIgnoreCase(link)) {
            return new InfoErrorDetail(owner, result.getErrorMessages(), result.getInfoMessages(),
                    labelProvider.getName());
        }
        for (Severity severity : Severity.getPredefinedValues()) {
            if (severity.getName().equalsIgnoreCase(link)) {
                Predicate<Issue> severityFilter = Issue.bySeverity(severity);
                return new IssuesDetail(owner, result,
                        allIssues.filter(severityFilter), newIssues.filter(severityFilter),
                        outstandingIssues.filter(severityFilter), fixedIssues.filter(severityFilter),
                        LocalizedSeverity.getLongLocalizedString(severity), url,
                        labelProvider, sourceEncoding);
            }
        }
        throw new NoSuchElementException("There is no URL mapping for %s and %s", parent.getUrl(), link);
    }

    private Predicate<Issue> createPropertyFilter(final String plainLink, final String property) {
        return issue -> plainLink.equals(String.valueOf(
                Issue.getPropertyValueAsString(issue, property).hashCode()));
    }

    private String getDisplayNameOfDetails(final String property, final Report selectedIssues) {
        return getColumnHeaderFor(selectedIssues, property)
                + " "
                + getPropertyValueAsString(property, selectedIssues);
    }

    private String getPropertyValueAsString(final String property, final Report selectedIssues) {
        if ("fileName".equals(property)) {
            return selectedIssues.get(0).getBaseName();
        }
        
        return Issue.getPropertyValueAsString(selectedIssues.get(0), property);
    }

    private String removePropertyPrefix(final String link) {
        return StringUtils.substringAfter(link, ".");
    }

    /**
     * Returns the localized column header of the specified property name.
     *
     * @param propertyName
     *         the name of the property
     *
     * @return the function that obtains the value
     */
    private String getColumnHeaderFor(final Report report, final String propertyName) {
        try {
            return PropertyUtils.getProperty(new TabLabelProvider(report), propertyName).toString();
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            return "Element";
        }
    }
}
