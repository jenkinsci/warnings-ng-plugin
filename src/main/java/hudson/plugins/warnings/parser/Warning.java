package hudson.plugins.warnings.parser;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;

import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A serializable Java Bean class representing a warning.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * </p>
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class Warning extends AbstractAnnotation {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5171661552905752370L;
    /** Origin of the annotation. */
    public static final String ORIGIN = "warnings";

    /** Additional warning description. Might be empty. @since 4.8 */
    private String toolTip;

    /**
     * Creates a new instance of {@link Warning}.
     *
     * @param fileName
     *            the name of the file
     * @param start
     *            the first line of the line range
     * @param type
     *            the identifier of the warning type
     * @param category
     *            the warning category
     * @param message
     *            the message of the warning
     */
    @Whitelisted
    public Warning(final String fileName, final int start, final String type, final String category, final String message) {
        this(fileName, start, type, category, message, Priority.NORMAL);
    }

    /**
     * Creates a new instance of <code>Warning</code>.
     *
     * @param fileName
     *            the name of the file
     * @param start
     *            the first line of the line range
     * @param type
     *            the identifier of the warning type
     * @param category
     *            the warning category
     * @param message
     *            the message of the warning
     * @param priority
     *            the priority of the warning
     */
    @Whitelisted
    public Warning(final String fileName, final int start, final String type, final String category, final String message, final Priority priority) {
        super(priority, message, start, start, category, type);

        setFileName(fileName);
        setOrigin(ORIGIN);
    }

    /**
     * Creates a new instance of {@link Warning}. This warning is a copy of the
     * specified warning with the additional message text (at the specified
     * line).
     *
     * @param copy
     *            the warning to copy
     * @param additionalMessage
     *            the additional message text
     * @param currentLine
     *            the current line
     */
    @Whitelisted
    public Warning(final FileAnnotation copy, final String additionalMessage, final int currentLine) {
        super(copy.getPriority(), copy.getMessage() + "\n" + additionalMessage,
                copy.getPrimaryLineNumber(), currentLine, copy.getCategory(), copy.getType());

        setFileName(copy.getFileName());
        setOrigin(ORIGIN);
    }

    public Warning(final FileAnnotation copy, final int currentLine) {
        super(copy.getPriority(), copy.getMessage(), copy.getPrimaryLineNumber(), currentLine, copy.getCategory(),
                copy.getType());
        setFileName(copy.getFileName());
        setOrigin(ORIGIN);
    }

    @Override
    public String getToolTip() {
        return StringUtils.defaultString(toolTip);
    }

    /**
     * Sets the tool tip for this warning to the specified value.
     *
     * @param value the value to set
     */
    public void setToolTip(final String value) {
        toolTip = value;
    }
}

