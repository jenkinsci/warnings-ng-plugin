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
     * Returns a detail object for the selected element of the specified annotation allIssues. The details will include
     * the new and fixed warnings trends as well as the errors report.
     *
     * @param link
     *         the link to identify the sub page to show
     * @param owner
     *         the build as owner of the detail page
     * @param allIssues
     *         the annotation allIssues to get the details for
     * @param fixedIssues
     *         the annotations fixed in this build
     * @param newIssues
     *         the annotations new in this build
     * @param errors
     *         the errors in this build
     * @param defaultEncoding
     *         the default encoding to be used when reading and parsing files
     * @param parent
     *         the name of the selected object
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
                    Messages._NewWarningsDetail_Name());
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
        else if (link.startsWith("moduleName.")) {
            Predicate<Issue> moduleFilter = issue -> issue.getModuleName().equals(plainLink);
            return new IssuesDetail(owner,
                    allIssues.filter(moduleFilter), fixedIssues.filter(moduleFilter), newIssues.filter(moduleFilter),
                    defaultEncoding, parent, Messages._ModuleDetail_header());
        }
        else if (link.startsWith("packageName.")) {
            Predicate<Issue> packageFilter = issue -> issue.getPackageName().equals(plainLink);
            return new IssuesDetail(owner,
                    allIssues.filter(packageFilter), fixedIssues.filter(packageFilter), newIssues.filter(packageFilter),
                    defaultEncoding, parent, Messages._PackageDetail_header());
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
        return null;
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
                defaultEncoding, parent, LocalizedPriority.getLongLocalized(priority));
    }

}
