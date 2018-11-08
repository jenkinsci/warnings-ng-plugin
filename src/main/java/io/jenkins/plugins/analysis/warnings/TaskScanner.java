package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import hudson.AbortException;

/**
 * Scans a given input stream for open tasks.
 *
 * @author Ulli Hafner
 */
public class TaskScanner {
    private static final String WORD_BOUNDARY = "\\b";

    /** The regular expression patterns to be used to scan the files. One pattern per priority. */
    private final Map<Severity, Pattern> patterns = new HashMap<Severity, Pattern>();
    private final boolean ignoreCase;

    private boolean isInvalidPattern;
    private final StringBuilder errorMessage = new StringBuilder();

    /**
     * Creates a new instance of {@link TaskScanner}.
     */
    public TaskScanner() {
        this("FIXME", "TODO", "@deprecated", false, false);
    }

    /**
     * Creates a new instance of {@link TaskScanner}.
     *
     * @param high
     *            tag identifiers indicating high priority
     * @param normal
     *            tag identifiers indicating normal priority
     * @param low
     *            tag identifiers indicating low priority
     * @param ignoreCase
     *            if case should be ignored during matching
     * @param asRegexp
     *            if tag identifiers should be treated as regular expression
     */
    public TaskScanner(final String high, final String normal, final String low,
                       final boolean ignoreCase, final boolean asRegexp) {
        this.ignoreCase = ignoreCase;
        if (StringUtils.isNotBlank(high)) {
            patterns.put(Severity.WARNING_HIGH, compile(high, ignoreCase, asRegexp));
        }
        if (StringUtils.isNotBlank(normal)) {
            patterns.put(Severity.WARNING_NORMAL, compile(normal, ignoreCase, asRegexp));
        }
        if (StringUtils.isNotBlank(low)) {
            patterns.put(Severity.WARNING_LOW, compile(low, ignoreCase, asRegexp));
        }
    }

    public boolean isInvalidPattern() {
        return isInvalidPattern;
    }

    public String getErrorMessage() {
        return errorMessage.toString();
    }

    /**
     * Compiles a regular expression pattern to scan for tag identifiers.
     *
     * @param tagIdentifiers
     *            the identifiers to scan for
     * @param ignoreCase
     *            specifies if case should be ignored
     * @param asRegexp
     *            if tag identifiers should be treated as regular expression
     * @return the compiled pattern
     */
    private Pattern compile(final String tagIdentifiers, final boolean ignoreCase, final boolean asRegexp) {
        try {
            if (asRegexp) {
                return Pattern.compile(tagIdentifiers);
            }

            String[] tags;
            if (tagIdentifiers.indexOf(',') == -1) {
                tags = new String[] {tagIdentifiers};
            }
            else {
                tags = StringUtils.split(tagIdentifiers, ",");
            }
            List<String> regexps = new ArrayList<String>();
            for (int i = 0; i < tags.length; i++) {
                String tag = tags[i].trim();
                if (StringUtils.isNotBlank(tag)) {
                    StringBuilder actual = new StringBuilder();
                    if (Character.isLetterOrDigit(tag.charAt(0))) {
                        actual.append(WORD_BOUNDARY);
                    }
                    actual.append(tag);
                    if (Character.isLetterOrDigit(tag.charAt(tag.length() - 1))) {
                        actual.append(WORD_BOUNDARY);
                    }
                    regexps.add(actual.toString());
                }
            }
            int flags;
            if (ignoreCase) {
                flags = Pattern.CASE_INSENSITIVE;
            }
            else {
                flags = 0;
            }
            return Pattern.compile("^.*(" + StringUtils.join(regexps.iterator(), "|") + ")(.*)$", flags);
        }
        catch (PatternSyntaxException exception) {
            isInvalidPattern = true;
            errorMessage.append(String.format("Specified pattern is an invalid regular expression: {%s}: {%s}", 
                    tagIdentifiers, exception.getMessage()));
            errorMessage.append('\n');

            return null;
        }
    }

    /**
     * Scans the specified input stream for open tasks.
     *
     * @param reader
     *            the file to scan
     * @return the result stored as java project
     * @throws IOException
     *             if we can't read the file
     */
    public Report scan(final Reader reader) throws IOException {
        try {
            if (isInvalidPattern) {
                throw new AbortException(errorMessage.toString());
            }
            LineIterator lineIterator = IOUtils.lineIterator(reader);
            Report report = new Report();

            IssueBuilder builder = new IssueBuilder();
            for (int lineNumber = 1; lineIterator.hasNext(); lineNumber++) {
                String line = (String)lineIterator.next();

                for (Severity severity : Severity.getPredefinedValues()) {
                    if (patterns.containsKey(severity)) {
                        Matcher matcher = patterns.get(severity).matcher(line);
                        if (matcher.matches() && matcher.groupCount() == 2) {
                            String message = matcher.group(2).trim();
                            String tag = matcher.group(1);
                            if (ignoreCase) {
                                tag = StringUtils.upperCase(tag);
                            }
                            Issue openTask = builder.setSeverity(severity)
                                    .setLineStart(lineNumber)
                                    .setType(tag)
                                    .setMessage(StringUtils.remove(message, ":").trim())
                                    .build();
                            report.add(openTask);
                        }
                    }
                }
            }

            return report;
        }
        finally {
            reader.close();
        }
    }
}

