package io.jenkins.plugins.analysis.core.views;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Priority;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.model.Item;
import hudson.model.Run;

/**
 * Creates detail objects for the selected link in the issues detail view. Each link might be visualized by a
 * specialized view.
 *
 * @author Ulli Hafner
 */
public class DetailFactory {
    private static final Report EMPTY = new Report();

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
    @SuppressWarnings({"ParameterNumber", "npathcomplexity", "PMD.CyclomaticComplexity"})
    public Object createTrendDetails(final String link, final Run<?, ?> owner, final AnalysisResult result,
            final Report allIssues, final Report newIssues,
            final Report outstandingIssues, final Report fixedIssues,
            final Charset sourceEncoding, final IssuesDetail parent) {
        StaticAnalysisLabelProvider labelProvider = parent.getLabelProvider();
        String plainLink = strip(link);
        String url = parent.getUrl() + "/" + plainLink;

        // FIXME: url is broken for new, high, etc.
        if ("fixed".equals(link)) {
            return new FixedWarningsDetail(owner, result, fixedIssues, url, labelProvider, sourceEncoding);
        }
        if ("new".equals(link)) {
            return new IssuesDetail(owner, result, newIssues, newIssues, EMPTY,
                    EMPTY, Messages.New_Warnings_Header(), url, labelProvider, sourceEncoding);
        }
        if ("outstanding".equals(link)) {
            return new IssuesDetail(owner, result, outstandingIssues, EMPTY, outstandingIssues,
                    EMPTY, Messages.Outstanding_Warnings_Header(), url, labelProvider, sourceEncoding);
        }
        if ("info".equals(link)) {
            return new InfoErrorDetail(owner, result.getErrorMessages(), result.getInfoMessages(), labelProvider.getName());
        }
        if (link.startsWith("source.")) {
            owner.checkPermission(Item.WORKSPACE);

            Issue issue = allIssues.findById(UUID.fromString(plainLink));
            if (ConsoleDetail.isInConsoleLog(issue)) {
                // FIXME: Put this in Jenkins Facade
                return new ConsoleDetail(owner, issue.getLineStart(), issue.getLineEnd());
            }
            else {
                // FIXME: Put this in Jenkins Facade
                return new SourceDetail(owner, issue, sourceEncoding);
            }
        }
        // FIXME: Do we need an error view?
        if (Priority.HIGH.equalsIgnoreCase(link)) {
            return createPrioritiesDetail(owner, result, Severity.WARNING_HIGH, allIssues, fixedIssues, outstandingIssues,
                    newIssues,
                    url, labelProvider, sourceEncoding);
        }
        if (Priority.NORMAL.equalsIgnoreCase(link)) {
            return createPrioritiesDetail(owner, result, Severity.WARNING_NORMAL, allIssues, fixedIssues, outstandingIssues,
                    newIssues,
                    url, labelProvider, sourceEncoding);
        }
        if (Priority.LOW.equalsIgnoreCase(link)) {
            return createPrioritiesDetail(owner, result, Severity.WARNING_LOW, allIssues, fixedIssues, outstandingIssues,
                    newIssues,
                    url, labelProvider, sourceEncoding);
        }

        String property = StringUtils.substringBefore(link, ".");
        Predicate<Issue> filter = createPropertyFilter(plainLink, property);
        Report selectedIssues = allIssues.filter(filter);
        if (selectedIssues.isEmpty()) {
            return parent; // fallback
        }
        else {
            return new IssuesDetail(owner, result,
                    selectedIssues, newIssues.filter(filter), outstandingIssues.filter(filter),
                    fixedIssues.filter(filter), getDisplayNameOfDetails(property, selectedIssues), url, labelProvider,
                    sourceEncoding);
        }
    }

    private Predicate<Issue> createPropertyFilter(final String plainLink, final String property) {
        return issue -> plainLink.equals(String.valueOf(
                Issue.getPropertyValueAsString(issue, property).hashCode()));
    }

    private String getDisplayNameOfDetails(final String property, final Report selectedIssues) {
        return getColumnHeaderFor(selectedIssues, property)
                + " "
                + Issue.getPropertyValueAsString(selectedIssues.get(0), property);
    }

    private String strip(final String link) {
        return StringUtils.substringAfter(link, ".");
    }

    @SuppressWarnings("ParameterNumber")
    private IssuesDetail createPrioritiesDetail(final Run<?, ?> owner,
            final AnalysisResult result, final Severity severity,
            final Report report, final Report fixedIssues, final Report newIssues,
            final Report outstandingIssues, final String url, final StaticAnalysisLabelProvider labelProvider,
            final Charset sourceEncoding) {
        Predicate<Issue> priorityFilter = issue -> issue.getSeverity() == severity;
        return new IssuesDetail(owner, result,
                report.filter(priorityFilter),
                newIssues.filter(priorityFilter),
                outstandingIssues.filter(priorityFilter),
                fixedIssues.filter(priorityFilter), LocalizedSeverity.getLongLocalizedString(severity), url,
                labelProvider, sourceEncoding);
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
