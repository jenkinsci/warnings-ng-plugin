package hudson.plugins.warnings.parser;

import hudson.ExtensionPoint;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.warnings.Messages;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;

/**
 * Parses an input stream for compiler warnings and returns the found
 * warnings. If your parser is based on a regular expression you can extend
 * from the existing base classes {@link RegexpLineParser} or
 * {@link RegexpDocumentParser}.
 *
 * @see RegexpLineParser Parses files line by line
 * @see RegexpDocumentParser Parses files using mulit-line regular expression
 * @see GccParser example
 * @see JavacParser example
 *
 * @author Ulli Hafner
 * @since 4.0
 */
public abstract class AbstractWarningsParser implements ExtensionPoint, Serializable {
    private static final long serialVersionUID = 8466657735514387654L;

    private final Localizable parserName;
    private final Localizable linkName;
    private final Localizable trendName;
    private final String name;

    /**
     * Creates a {@link Localizable} for the specified string value.
     *
     * @param string
     *            the string to wrap
     * @return localized string
     */
    protected static Localizable localize(final String string) {
        return hudson.plugins.warnings.parser.Messages._Warnings_NotLocalizedName(string);
    }

    /**
     * Creates a new instance of {@link AbstractWarningsParser}.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    protected AbstractWarningsParser(final String parserName, final String linkName, final String trendName) {
        this(localize(parserName), localize(linkName), localize(trendName));
    }

    /**
     * Creates a new instance of {@link AbstractWarningsParser}.
     *
     * @param parserName
     *            name of the parser
     */
    protected AbstractWarningsParser(final String parserName) {
        this(localize(parserName), Messages._Warnings_ProjectAction_Name(), Messages._Warnings_Trend_Name());
    }

    /**
     * Creates a new instance of {@link AbstractWarningsParser}.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     */
    protected AbstractWarningsParser(final Localizable parserName, final Localizable linkName, final Localizable trendName) {
        this.parserName = parserName;
        this.linkName = linkName;
        this.trendName = trendName;
        name = parserName.toString(Locale.ENGLISH);
    }

    /**
     * Parses the specified input stream for compiler warnings and returns the
     * found annotations. Note that the implementor of this method must not
     * close the given reader, this is done by the framework.
     *
     * @param reader
     *            the reader to get the text from
     * @return the collection of annotations
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParsingCanceledException
     *             Signals that the user canceled this operation
     */
    public abstract Collection<FileAnnotation> parse(final Reader reader) throws IOException, ParsingCanceledException;

    /**
     * Gets the human readable name of this parser. This name is shown in the
     * configuration screen of a job. If more parsers share the same name (using
     * the English locale) then these parsers are considered as a group.
     * Configuration, visualization and reporting of parsers is always based on
     * the associated group.
     *
     * @return the name of parser
     */
    public String getParserName() {
        if (parserName == null) {
            return name;
        }
        return parserName.toString();
    }

    /**
     * Gets the human readable name of this parser. This name is shown as link
     * in Jenkin's project view, and as title in the project summary of the
     * warnings plug-in.
     *
     * @return the name of parser
     */
    public Localizable getLinkName() {
        return linkName;
    }

    /**
     * Gets the human readable name of the trend report for this parser.
     *
     * @return the name of parser
     */
    public Localizable getTrendName() {
        return trendName;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns whether this parser is in the specified group.
     *
     * @param group the name of the group
     * @return <code>true</code> if this parser is in the specified group
     */
    public boolean isInGroup(final String group) {
        return name.equals(group) || getId().equals(group);
    }

    /**
     * Returns the group of this parser. Multiple parsers can share the same
     * group in order to simplify the user interface.
     *
     * @return the group of this parser
     */
    public String getGroup() {
        return name;
    }

    /**
     * Returns the ID of this parser. Normally, there is no need to override
     * this method since parser matching is based on the {@code group}. This
     * method has been introduced to ensure backward compatibility.
     *
     * @return the ID of this parser
     */
    protected String getId() {
        return name;
    }

    /**
     * Creates a new instance of {@link Warning} using the parser's group as
     * warning type.
     *
     * @param fileName
     *            the name of the file
     * @param start
     *            the first line of the line range
     * @param category
     *            the warning category
     * @param message
     *            the message of the warning
     * @return the warning
     */
    public Warning createWarning(final String fileName, final int start, final String category, final String message) {
        return new Warning(fileName, start, getGroup(), category, message);
    }

    /**
     * Creates a new instance of {@link Warning} using the parser's group as
     * warning type. No category will be set.
     *
     * @param fileName
     *            the name of the file
     * @param start
     *            the first line of the line range
     * @param message
     *            the message of the warning
     * @return the warning
     */
    public Warning createWarning(final String fileName, final int start, final String message) {
        return createWarning(fileName, start, StringUtils.EMPTY, message);
    }

    /**
     * Creates a new instance of {@link Warning} using the parser's group as warning type.
     *
     * @param fileName
     *            the name of the file
     * @param start
     *            the first line of the line range
     * @param category
     *            the warning category
     * @param message
     *            the message of the warning
     * @param priority
     *            the priority of the warning
     * @return the warning
     */
    public Warning createWarning(final String fileName, final int start, final String category, final String message, final Priority priority) {
        return new Warning(fileName, start, getGroup(), category, message, priority);
    }

    /**
     * Creates a new instance of {@link Warning} using the parser's group as
     * warning type. No category will be set.
     *
     * @param fileName
     *            the name of the file
     * @param start
     *            the first line of the line range
     * @param message
     *            the message of the warning
     * @param priority
     *            the priority of the warning
     * @return the warning
     */
    public Warning createWarning(final String fileName, final int start, final String message, final Priority priority) {
        return createWarning(fileName, start, StringUtils.EMPTY, message, priority);
    }
}

