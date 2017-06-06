package hudson.plugins.analysis.util.model;

import java.io.Serializable;

import hudson.plugins.analysis.Messages;

/**
 * Provides localized labels for the different categories of annotations stored in an annotation container.
 *
 * @author Ullrich Hafner
 */
public class AnnotationsLabelProvider implements Serializable {
    private static final long serialVersionUID = -4942733658741742463L;

    private final String packageLabel;

    public AnnotationsLabelProvider() {
        this(Messages.PackageDetail_title());
    }

    public AnnotationsLabelProvider(final String packageLabel) {
        this.packageLabel = packageLabel;
    }

    public String getModules() {
        return Messages.BuildResult_Tab_Modules();
    }

    public String getAuthors() {
        return Messages.BuildResult_Tab_Authors();
    }

    public String getWarnings() {
        return Messages.BuildResult_Tab_Warnings();
    }

    public String getPackages() { return packageLabel; }

    public String getFiles() {
        return Messages.BuildResult_Tab_Files();
    }

    public String getCategories() {
        return Messages.BuildResult_Tab_Categories();
    }

    public String getTypes() {
        return Messages.BuildResult_Tab_Types();
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
}
