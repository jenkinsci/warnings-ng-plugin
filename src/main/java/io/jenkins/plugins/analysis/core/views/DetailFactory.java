package io.jenkins.plugins.analysis.core.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.beanutils.PropertyUtils;
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
            return new NewWarningsDetail(owner, newIssues, defaultEncoding, parent);
        }
        else if ("error".equals(link)) {
            return new ErrorDetail(owner, errors, parent);
        }
        else if (link.startsWith("source.")) {
            owner.checkPermission(Item.WORKSPACE);

            Issue issue = allIssues.findById(UUID.fromString(plainLink));
            if (issue.getFileName().equals(ConsoleDetail.CONSOLE_LOG_FILENAME)) {
                return new ConsoleDetail(owner, issue.getLineStart(), issue.getLineEnd());
            }
            else {
                return new SourceDetail(owner, issue, defaultEncoding);
            }
        }
        else if (Priority.HIGH.equalsIgnoreCase(plainLink)) {
            return createPrioritiesDetail(Priority.HIGH, owner, allIssues, fixedIssues, newIssues, defaultEncoding, parent);
        }
        else if (Priority.NORMAL.equalsIgnoreCase(plainLink)) {
            return createPrioritiesDetail(Priority.NORMAL, owner, allIssues, fixedIssues, newIssues, defaultEncoding, parent);
        }
        else if (Priority.LOW.equalsIgnoreCase(plainLink)) {
            return createPrioritiesDetail(Priority.LOW, owner, allIssues, fixedIssues, newIssues, defaultEncoding, parent);
        }
        else if (link.startsWith("module.")) {
            Predicate<Issue> moduleFilter = issue -> issue.getModuleName().equals(StringUtils.substringAfter(link, "module."));
            return new IssuesDetail(owner,
                    allIssues.filter(moduleFilter), fixedIssues.filter(moduleFilter), newIssues.filter(moduleFilter),
                    defaultEncoding, parent, Messages._ModuleDetail_header());
        }
        else if ("tab.fileName".equals(link)) {
            return new TabDetail(owner, allIssues, createGenericTabUrl(link), defaultEncoding, parent, issue -> issue.getFileName(),
                    string -> StringUtils.substringAfterLast(string, "/"));
        }
        else if (link.startsWith("tab.")) {
            return new TabDetail(owner, allIssues, createGenericTabUrl(link), defaultEncoding, parent, getIssueStringFunction(plainLink),
                    Function.identity());
        }
//        else if (link.startsWith("package.")) {
//            return new PackageDetail(owner, this, allIssues.getPackage(createHashCode(link, "package.")), defaultEncoding, parent);
//        }
//        else if (link.startsWith("file.")) {
//            return new FileDetail(owner, this, allIssues.getFile(createHashCode(link, "file.")), defaultEncoding, parent);
//        }
//        else if (link.startsWith("category.")) {
//            DefaultAnnotationContainer category = allIssues.getCategory(createHashCode(link, "category."));
//            return createAttributeDetail(owner, category, parent, Messages.CategoryDetail_header(), defaultEncoding);
//        }
//        else if (link.startsWith("type.")) {
//            DefaultAnnotationContainer type = allIssues.getType(createHashCode(link, "type."));
//            return createAttributeDetail(owner, type, parent, Messages.TypeDetail_header(), defaultEncoding);
//        }
//        else if (link.startsWith("author.")) {
//            return new AuthorDetail(owner, this, allIssues.getAuthor(createHashCode(link, "author.")), defaultEncoding, parent);
//        }
        return null;
    }

    private Function<Issue, String> getIssueStringFunction(final String plainLink) {
        return issue -> {
            try {
                return PropertyUtils.getProperty(issue, plainLink).toString();
            }
            catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                return plainLink;
            }
        };
    }

    private String strip(final String link) {
        return StringUtils.substringAfter(link, ".");
    }

    /**
     * Creates a new priorities detail.
     *
     * @param priority
     *         the priority to show
     * @param owner
     *         owner of the build
     * @param issues
     *         annotation issues
     */
    protected IssuesDetail createPrioritiesDetail(final Priority priority, final Run<?, ?> owner,
            final Issues issues, final Issues fixedIssues, final Issues newIssues, final String defaultEncoding, final ModelObject parent) {
        Predicate<Issue> priorityFilter = issue -> issue.getPriority() == priority;
        return new IssuesDetail(owner, issues.filter(priorityFilter), fixedIssues.filter(priorityFilter), newIssues.filter(priorityFilter),
                defaultEncoding, parent, LocalizedPriority.getLongLocalized(priority));
    }

    // FIXME: what to do with the label provider
    /**
     * Returns the default label provider that is used to visualize the build result (i.e., the tab labels).
     *
     * @return the default label provider
     * @since 1.69
     */
//    protected void attachLabelProvider(final AnnotationContainer container) {
//        container.setLabelProvider(new AnnotationsLabelProvider(container.getPackageCategoryTitle()));
//    }

    /**
     * Creates the actual URL from the synthetic link.
     *
     * @param link
     *         the link
     *
     * @return the actual URL
     */
    private String createGenericTabUrl(final String link) {
        return StringUtils.substringAfter(link, "tab.") + ".jelly";
    }

    /**
     * Extracts the hash code from the given link stripping of the given prefix.
     *
     * @param link
     *         the whole link
     * @param prefix
     *         the prefix to remove
     *
     * @return the hash code
     */
    private int createHashCode(final String link, final String prefix) {
        try {
            return Integer.parseInt(StringUtils.substringAfter(link, prefix));
        }
        catch (NumberFormatException e) {
            return -1; // non-existent ID
        }
    }
}
