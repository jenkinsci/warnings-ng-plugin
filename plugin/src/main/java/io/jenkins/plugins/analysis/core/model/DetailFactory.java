package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.BuildFolderFacade;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.bootstrap5.MessagesViewModel;
import io.jenkins.plugins.prism.Marker;
import io.jenkins.plugins.prism.Marker.MarkerBuilder;
import io.jenkins.plugins.prism.SourceCodeViewModel;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Creates detail objects for the selected link in the issues detail view. Each link might be visualized by a
 * specialized view.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity"})
public class DetailFactory {
    private static final Report EMPTY = new Report();
    private static final String LINK_SEPARATOR = ".";

    private final JenkinsFacade jenkins;
    private final BuildFolderFacade buildFolder;

    /**
     * Creates a new instance of {@link DetailFactory}.
     */
    public DetailFactory() {
        this(new JenkinsFacade(), new BuildFolderFacade());
    }

    @VisibleForTesting
    DetailFactory(final JenkinsFacade jenkinsFacade, final BuildFolderFacade buildFolder) {
        jenkins = jenkinsFacade;
        this.buildFolder = buildFolder;
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
    @SuppressWarnings({"PMD.CyclomaticComplexity", "checkstyle:ParameterNumber"})
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

    @SuppressWarnings("checkstyle:ParameterNumber")
    private Object createFilteredView(final String link, final Run<?, ?> owner, final AnalysisResult result,
            final Report allIssues, final Report newIssues, final Report outstandingIssues, final Report fixedIssues,
            final Charset sourceEncoding, final IssuesDetail parent, final StaticAnalysisLabelProvider labelProvider) {
        String plainLink = removePropertyPrefix(link);
        if (link.startsWith("source.")) {
            Issue issue = allIssues.findById(UUID.fromString(plainLink));
            if (ConsoleLogHandler.isInConsoleLog(issue.getFileName())) {
                try (Stream<String> consoleLog = buildFolder.readConsoleLog(owner)) {
                    return new ConsoleDetail(owner, consoleLog, issue.getLineStart(), issue.getLineEnd());
                }
            }
            else {
                Marker marker = asMarker(issue, labelProvider.getSourceCodeDescription(owner, issue), labelProvider.getSmallIconUrl());
                try (Reader affectedFile = buildFolder.readFile(owner, issue.getFileName(), sourceEncoding)) {
                    return new SourceCodeViewModel(owner, issue.getBaseName(), affectedFile, marker);
                }
                catch (IOException e) {
                    try (StringReader fallback = new StringReader(
                            String.format("%s%n%s", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e)))) {
                        return new SourceCodeViewModel(owner, issue.getBaseName(), fallback, marker);
                    }
                }
            }
        }

        String url = parent.getUrl() + "/" + plainLink;
        String property = StringUtils.substringBefore(link, ".");
        Predicate<Issue> filter = createPropertyFilter(plainLink, property);
        Report selectedIssues = allIssues.filter(filter);
        String displayName = getDisplayNameOfDetails(property, selectedIssues, plainLink,
                result.getSizePerOrigin().keySet());
        return new IssuesDetail(owner, result,
                selectedIssues, newIssues.filter(filter), outstandingIssues.filter(filter),
                fixedIssues.filter(filter), displayName, url,
                labelProvider, sourceEncoding);
    }

    private Marker asMarker(final Issue issue, final String description, final String icon) {
        return new MarkerBuilder()
                .withTitle(issue.getMessage())
                .withDescription(description)
                .withIcon(icon)
                .withLineStart(issue.getLineStart())
                .withLineEnd(issue.getLineEnd())
                .withColumnStart(issue.getColumnStart())
                .withColumnEnd(issue.getColumnEnd()).build();
    }

    @SuppressWarnings({"checkstyle:ParameterNumber", "PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    @SuppressFBWarnings("IMPROPER_UNICODE")
    private Object createNewDetailView(final String link, final Run<?, ?> owner, final AnalysisResult result,
            final Report allIssues, final Report newIssues, final Report outstandingIssues, final Report fixedIssues,
            final Charset sourceEncoding, final IssuesDetail parent, final StaticAnalysisLabelProvider labelProvider) {
        String url = parent.getUrl() + "/" + link;

        if ("all".equalsIgnoreCase(link)) {
            return new IssuesDetail(owner, result, allIssues, newIssues, outstandingIssues, fixedIssues,
                    labelProvider.getLinkName(), url, labelProvider, sourceEncoding);
        }
        if ("modified".equalsIgnoreCase(link)) {
            return new IssuesDetail(owner, result, filterModified(allIssues), filterModified(newIssues),
                    filterModified(outstandingIssues), EMPTY,
                    Messages.Modified_Warnings_Header(), url, labelProvider, sourceEncoding);
        }
        if ("unchanged".equalsIgnoreCase(link)) {
            return new IssuesDetail(owner, result, filterUnchanged(allIssues), filterUnchanged(newIssues),
                    filterUnchanged(outstandingIssues), EMPTY,
                    Messages.Modified_Warnings_Header(), url, labelProvider, sourceEncoding);
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
            return new MessagesViewModel(owner, labelProvider.getName(),
                    result.getInfoMessages().castToList(),
                    result.getErrorMessages().castToList());
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
        throw new NoSuchElementException(String.format("There is no URL mapping for %s and %s", parent.getUrl(), link));
    }

    private Report filterModified(final Report report) {
        return report.filter(Issue::isPartOfModifiedCode);
    }

    private Report filterUnchanged(final Report report) {
        return report.filter(Predicate.not(Issue::isPartOfModifiedCode));
    }

    private Predicate<Issue> createPropertyFilter(final String plainLink, final String property) {
        return issue -> plainLink.equals(String.valueOf(
                Issue.getPropertyValueAsString(issue, property).hashCode()));
    }

    @SuppressFBWarnings(value = "UNSAFE_HASH_EQUALS", justification = "Hashcode is used as URL")
    private String getDisplayNameOfDetails(final String property, final Report selectedIssues,
            final String originHash, final Set<String> origins) {
        if ("origin".equals(property)) {
            LabelProviderFactory factory = createFactory();
            for (String origin : origins) {
                if (String.valueOf(origin.hashCode()).equals(originHash)) {
                    return factory.create(origin).getName();
                }
            }
            return factory.create(StringUtils.EMPTY).getName();
        }
        return getColumnHeaderFor(selectedIssues, property)
                + " "
                + getPropertyValueAsString(property, selectedIssues);
    }

    @VisibleForTesting
    private LabelProviderFactory createFactory() {
        return new LabelProviderFactory(jenkins);
    }

    private String getPropertyValueAsString(final String property, final Report selectedIssues) {
        if (selectedIssues.isEmpty()) {
            return "n/a";
        }
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
     * @param report
     *         the report to get the header for
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
