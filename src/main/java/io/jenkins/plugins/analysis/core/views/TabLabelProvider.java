package io.jenkins.plugins.analysis.core.views;

import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issues;

import hudson.plugins.analysis.Messages;

/**
 * Provides localized labels for the different categories of annotations stored in an annotation container.
 *
 * @author Ullrich Hafner
 */
public class TabLabelProvider {
    private final Issues issues;

    public TabLabelProvider(final Issues issues) {
        this.issues = issues;
    }

    public String getAuthors() {
        return Messages.BuildResult_Tab_Authors();
    }

    public String getWarnings() {
        return Messages.BuildResult_Tab_Warnings();
    }

    public String getOrigin() {
        return Messages.BuildResult_Tab_Origin();
    }

    public String getModules() {
        return Messages.BuildResult_Tab_Modules();
    }

    public String getModuleName() {
        return Messages.BuildResult_Tab_Module();
    }

    public String getFiles() {
        return Messages.BuildResult_Tab_Files();
    }

    public String getFileName() {
        return Messages.BuildResult_Tab_File();
    }

    public String getCategories() {
        return Messages.BuildResult_Tab_Categories();
    }

    public String getCategory() {
        return Messages.BuildResult_Tab_Category();
    }

    public String getTypes() {
        return Messages.BuildResult_Tab_Types();
    }

    public String getType() {
        return Messages.BuildResult_Tab_Type();
    }

    public String getDetails() {
        return Messages.BuildResult_Tab_Details();
    }

    public String getNew() {
        return Messages.BuildResult_Tab_New();
    }

    public String getFixed() {
        return Messages.BuildResult_Tab_Fixed();
    }

    public String getHigh() {
        return Messages.BuildResult_Tab_High();
    }

    public String getNormal() {
        return Messages.BuildResult_Tab_Normal();
    }

    public String getLow() {
        return Messages.BuildResult_Tab_Low();
    }

    /**
     * Returns the package column title using the suffix of the affected files.
     *
     * @return the package column title
     */
    public final String getPackageName() {
        return getPackageOrNamespace(Messages.PackageDetail_header(), Messages.NamespaceDetail_header());
    }

   /**
     * Returns the package category title using the suffix of the affected files.
     *
     * @return the package category title
     */
    public final String getPackages() {
        return getPackageOrNamespace(Messages.PackageDetail_title(), Messages.NamespaceDetail_title());
    }

    private String getPackageOrNamespace(final String packageText, final String nameSpaceText) {
        if (issues.isNotEmpty()) {
            SortedSet<String> fileTypes = issues.getProperties(issue -> StringUtils.substringAfterLast(issue.getFileName(), "."));
            if (fileTypes.contains("cs")) {
                return nameSpaceText;
            }
        }
        return packageText;
    }
}
