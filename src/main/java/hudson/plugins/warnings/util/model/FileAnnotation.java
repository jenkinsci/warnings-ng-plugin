package hudson.plugins.warnings.util.model;

import java.util.Collection;

/**
 * Annotates a collection of line ranges in a file. An annotation consists of a
 * description and a tooltip.
 *
 * @author Ulli Hafner
 */
public interface FileAnnotation {
    /**
     * Returns the message of this annotation.
     *
     * @return the message of this annotation
     */
    String getMessage();

    /**
     * Returns the a detailed description that will be used as tooltip.
     *
     * @return the tooltip of this annotation
     */
    String getToolTip();

    /**
     * Returns the primary line number of this annotation that defines the
     * anchor of this annotation.
     *
     * @return the primary line number of this annotation
     */
    int getPrimaryLineNumber();

    /**
     * Returns a collection of line ranges for this annotation.
     *
     * @return the collection of line ranges for this annotation.
     */
    Collection<LineRange> getLineRanges();

    /**
     * Returns the unique key of this annotation.
     *
     * @return the unique key of this annotation.
     */
    long getKey();

    /**
     * Returns the priority of this annotation.
     *
     * @return the priority of this annotation
     */
    Priority getPriority();

    /**
     * Returns the absolute path of the workspace file that contains this annotation.
     *
     * @return the name of the workspace file that contains this annotation
     */
    String getFileName();

    /**
     * Returns the name of the maven or ant module that contains this annotation.
     *
     * @return the name of the module that contains this annotation
     */
    String getModuleName();

    /**
     * Returns the name of package (or namespace) that contains this annotation.
     *
     * @return the name of the package that contains this annotation
     */
    String getPackageName();

    /**
     * Returns the category of the annotation. Might be an empty string if there is no category.
     *
     * @return the annotation category
     */
    String getCategory();

    /**
     * Returns the annotation type. Might be an empty string if there is no type.
     *
     * @return the annotation type
     */
    String getType();
}
