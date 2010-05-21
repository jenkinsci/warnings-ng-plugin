package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for Perforce execution.
 *
 * @author Adrián Deccico
 */
public class P4Parser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Perforce Compiler";
    /** Sub-Pattern of perforce compiler warnings. */
    static final String ALREADY_OPENED = "already opened for edit";
    /** Sub-Pattern of perforce compiler warnings. */
    static final String CANT_ADD = "can't add existing file";
    /** Sub-Pattern of perforce compiler warnings. */
    static final String WARNING_ADD_OF = "warning: add of existing file";
    /** Sub-Pattern of perforce compiler warnings. */
    static final String OPENED_FOR_EDIT = "can't add \\(" + ALREADY_OPENED + "\\)";
    /** Sub-Pattern of perforce compiler warnings. */
    static final String NOTHING_CHANGED = "nothing changed";

    /** Pattern of perforce compiler warnings. */
    private static final String PERFORCE_WARNING_PATTERN =
                                                            "^(.*) - "
                                                            + "("
                                                            + CANT_ADD + "|"
                                                            + WARNING_ADD_OF + "|"
                                                            + OPENED_FOR_EDIT + "|"
                                                            + NOTHING_CHANGED
                                                            + ")"
                                                            + "(.*)$";
    /**
     * Creates a new instance of {@link P4Parser}.
     */
    public P4Parser() {
        super(PERFORCE_WARNING_PATTERN, WARNING_TYPE, true);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher
     *            the regular expression matcher
     * @return a new annotation for the specified pattern
     *
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String category = matcher.group(2).trim();
        String fileName = matcher.group(1).trim();
        String message = fileName;
        Priority p = Priority.NORMAL;
        if (category.contains(ALREADY_OPENED) || category.equals(NOTHING_CHANGED)) {
            p = Priority.LOW;
        }
        return new Warning(fileName, 0, WARNING_TYPE, category, message, p);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains(" - ");
    }
}

