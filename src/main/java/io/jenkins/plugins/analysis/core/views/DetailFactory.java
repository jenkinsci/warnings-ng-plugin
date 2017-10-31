package io.jenkins.plugins.analysis.core.views;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;

import hudson.model.Item;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;

/**
 * Creates detail objects for the selected element of a annotation container.
 *
 * @author Ulli Hafner
 */
public class DetailFactory {
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
     * @return the dynamic result of this module detail view
     */
    public Object createTrendDetails(final String link, final Run<?, ?> owner,
            final Issues allIssues, final Issues fixedIssues,
            final Issues newIssues, final Collection<String> errors,
            final String defaultEncoding, final ModelObject parent) {
        String plainLink = strip(link);
        if ("fixed".equals(link)) {
            return new FixedWarningsDetail(owner, fixedIssues, defaultEncoding, parent);
        }
        else if ("new".equals(link)) {
            return new IssuesDetail(owner, newIssues, new Issues(), newIssues, defaultEncoding, parent,
                    Messages.NewWarningsDetail_Name());
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
            return createPrioritiesDetail(Priority.HIGH, owner, allIssues, fixedIssues, newIssues, defaultEncoding, parent);
        }
        else if (Priority.NORMAL.equalsIgnoreCase(link)) {
            return createPrioritiesDetail(Priority.NORMAL, owner, allIssues, fixedIssues, newIssues, defaultEncoding, parent);
        }
        else if (Priority.LOW.equalsIgnoreCase(link)) {
            return createPrioritiesDetail(Priority.LOW, owner, allIssues, fixedIssues, newIssues, defaultEncoding, parent);
        }
        else if (link.startsWith("tab.table")) {
            return new IssuesTableTab(owner, allIssues, defaultEncoding, parent);
        }
        else if (link.startsWith("tab.details")) {
            return new IssuesDetailTab(owner, allIssues, defaultEncoding, parent);
        }
        else if (link.startsWith("tab.")) {
            Function<String, String> propertyFormatter;
            if ("fileName".equals(plainLink)) {
                propertyFormatter = IssuesDetail.FILE_NAME_FORMATTER;
            }
            else {
                propertyFormatter = Function.identity();
            }
            return new PropertyCountTab(owner, allIssues, defaultEncoding, parent, plainLink, propertyFormatter);
        }
        else {
            String property = StringUtils.substringBefore(link, ".");
            Predicate<Issue> filter = createPropertyFilter(plainLink, property);
            Issues selectedIssues = allIssues.filter(filter);
            return new IssuesDetail(owner,
                    selectedIssues, fixedIssues.filter(filter), newIssues.filter(filter),
                    defaultEncoding, parent, getDisplayNameOfDetails(property, selectedIssues));
        }
    }

    private Predicate<Issue> createPropertyFilter(final String plainLink, final String property) {
        return issue -> plainLink.equals(String.valueOf(
                PropertyCountTab.getIssueStringFunction(property).apply(issue).hashCode()));
    }

    private String getDisplayNameOfDetails(final String property, final Issues selectedIssues) {
        return PropertyCountTab.getColumnHeaderFor(selectedIssues, property)
                + " "
                + PropertyCountTab.getIssueStringFunction(property).apply(selectedIssues.get(0));
    }

    private String strip(final String link) {
        return StringUtils.substringAfter(link, ".");
    }

    private IssuesDetail createPrioritiesDetail(final Priority priority, final Run<?, ?> owner,
            final Issues issues, final Issues fixedIssues, final Issues newIssues,
            final String defaultEncoding, final ModelObject parent) {
        Predicate<Issue> priorityFilter = issue -> issue.getPriority() == priority;
        return new IssuesDetail(owner,
                issues.filter(priorityFilter), fixedIssues.filter(priorityFilter), newIssues.filter(priorityFilter),
                defaultEncoding, parent, LocalizedPriority.getLongLocalizedString(priority));
    }

}
