package io.jenkins.plugins.analysis.core.views;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.model.Item;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;

/**
 * Creates detail objects for the selected link in an issues detail view.
 *
 * @author Ulli Hafner
 */
public class DetailFactory {
    private static final Issues<Issue> EMPTY = new Issues<>();

    /**
     * Returns a detail object for the selected element for the specified issues.
     *
     * @param link
     *         the link to identify the sub page to show
     * @param owner
     *         the build as owner of the detail page
     * @param allIssues
     *         the issues to get the details for
     * @param fixedIssues
     *         the fixed issues to get the details for
     * @param newIssues
     *         the new issues to get the details for
     * @param errors
     *         the errors during scanning the static analysis results
     * @param defaultEncoding
     *         the default encoding to be used when reading and parsing files
     * @param parent
     *         the parent of the selected object
     *
     * @param labelProvider
     * @return the dynamic result of this module detail view
     */
    public Object createTrendDetails(final String link, final Run<?, ?> owner,
            final Issues<Issue> allIssues, final Issues<Issue> fixedIssues,
            final Issues<Issue> newIssues, final Issues<Issue> outstandingIssues,
            final Collection<String> errors, final String defaultEncoding, final IssuesDetail parent) {
        StaticAnalysisLabelProvider labelProvider = parent.getLabelProvider();
        String plainLink = strip(link);
        String url = parent.getUrl() + "/" + plainLink;

        if ("fixed".equals(link)) {
            return new FixedWarningsDetail(owner, fixedIssues, defaultEncoding, labelProvider, url);
        }
        else if ("new".equals(link)) {
            return new IssuesDetail(owner, newIssues, newIssues, EMPTY, EMPTY, Messages.NewWarningsDetail_Name(), url,
                    labelProvider, defaultEncoding
            );
        }
        else if ("outstanding".equals(link)) {
            return new IssuesDetail(owner, outstandingIssues, EMPTY, outstandingIssues, EMPTY, "Outstanding Warnings", url,
                    labelProvider, defaultEncoding
            ); // l10n
        }
        else if ("error".equals(link)) {
            return new ErrorDetail(owner, errors, parent);
        }
        else if (link.startsWith("source.")) {
            owner.checkPermission(Item.WORKSPACE);

            Issue issue = allIssues.findById(UUID.fromString(plainLink));
            if (ConsoleDetail.isInConsoleLog(issue)) {
                return new ConsoleDetail(owner, issue.getLineStart(), issue.getLineEnd());
            }
            else {
                return new SourceDetail(owner, issue, defaultEncoding);
            }
        }
        else if (Priority.HIGH.equalsIgnoreCase(link)) {
            return createPrioritiesDetail(Priority.HIGH, owner, allIssues, fixedIssues, outstandingIssues, newIssues, defaultEncoding,
                    labelProvider, url);
        }
        else if (Priority.NORMAL.equalsIgnoreCase(link)) {
            return createPrioritiesDetail(Priority.NORMAL, owner, allIssues, fixedIssues, outstandingIssues, newIssues, defaultEncoding,
                    labelProvider, url);
        }
        else if (Priority.LOW.equalsIgnoreCase(link)) {
            return createPrioritiesDetail(Priority.LOW, owner, allIssues, fixedIssues, outstandingIssues, newIssues, defaultEncoding,
                    labelProvider, url);
        }
        else {
            String property = StringUtils.substringBefore(link, ".");
            Predicate<Issue> filter = createPropertyFilter(plainLink, property);
            Issues<Issue> selectedIssues = allIssues.filter(filter);
            return new IssuesDetail(owner,
                    selectedIssues, newIssues.filter(filter), outstandingIssues.filter(filter), fixedIssues.filter(filter),
                    getDisplayNameOfDetails(property, selectedIssues), url, labelProvider, defaultEncoding
            );
        }
    }

    private Predicate<Issue> createPropertyFilter(final String plainLink, final String property) {
        return issue -> plainLink.equals(String.valueOf(
                PropertyCountTab.getIssueStringFunction(property).apply(issue).hashCode()));
    }

    private String getDisplayNameOfDetails(final String property, final Issues<Issue> selectedIssues) {
        return PropertyCountTab.getColumnHeaderFor(selectedIssues, property)
                + " "
                + PropertyCountTab.getIssueStringFunction(property).apply(selectedIssues.get(0));
    }

    private String strip(final String link) {
        return StringUtils.substringAfter(link, ".");
    }

    private IssuesDetail createPrioritiesDetail(final Priority priority, final Run<?, ?> owner,
            final Issues<Issue> issues, final Issues<Issue> fixedIssues, final Issues<Issue> newIssues,
            final Issues<Issue> outstandingIssues, final String defaultEncoding,
            final StaticAnalysisLabelProvider labelProvider, final String url) {
        Predicate<Issue> priorityFilter = issue -> issue.getPriority() == priority;
        return new IssuesDetail(owner,
                issues.filter(priorityFilter), newIssues.filter(priorityFilter), outstandingIssues.filter(priorityFilter),
                fixedIssues.filter(priorityFilter),
                LocalizedPriority.getLongLocalizedString(priority), url, labelProvider, defaultEncoding
        );
    }

}
