package io.jenkins.plugins.analysis.warnings.tasks;

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
import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Scans a given input stream for open tasks.
 *
 * @author Ullrich Hafner
 */
public class TaskScanner {
    private static final String WORD_BOUNDARY = "\\b";
    private static final Pattern INVALID = Pattern.compile("");

    /** The regular expression patterns to be used to scan the files. One pattern per priority. */
    private final Map<Severity, Pattern> patterns = new HashMap<Severity, Pattern>();
    private final CaseMode caseMode;

    private boolean isInvalidPattern;
    private final StringBuilder errors = new StringBuilder();

    /** Determines whether the tags are case sensitive or not. */
    enum CaseMode {
        IGNORE_CASE, 
        CASE_SENSITIVE
    }
    
    /** Determines whether tags are plain strings or regular expressions. */
    enum MatcherMode {
        STRING_MATCH, 
        REGEXP_MATCH
    }
    
    /**
     * Creates a new instance of {@link TaskScanner}.
     *
     * @param high
     *            tag identifiers indicating tasks with high severity
     * @param normal
     *            tag identifiers indicating tasks with normal severity
     * @param low
     *            tag identifiers indicating low priority
     * @param caseMode
     *            if case should be ignored during matching
     * @param matcherMode
     *            if tag identifiers should be treated as regular expression
     */
    public TaskScanner(final @CheckForNull String high, final @CheckForNull String normal, final @CheckForNull String low,
                       final CaseMode caseMode, final MatcherMode matcherMode) {
        this.caseMode = caseMode;
        if (StringUtils.isNotBlank(high)) {
            patterns.put(Severity.WARNING_HIGH, compile(high, caseMode, matcherMode));
        }
        if (StringUtils.isNotBlank(normal)) {
            patterns.put(Severity.WARNING_NORMAL, compile(normal, caseMode, matcherMode));
        }
        if (StringUtils.isNotBlank(low)) {
            patterns.put(Severity.WARNING_LOW, compile(low, caseMode, matcherMode));
        }
    }

    public boolean isInvalidPattern() {
        return isInvalidPattern;
    }

    public String getErrors() {
        return errors.toString();
    }

    /**
     * Compiles a regular expression pattern to scan for tag identifiers.
     *
     * @param tagIdentifiers
     *            the identifiers to scan for
     * @param caseMode
     *            specifies if case should be ignored
     * @param matcherMode
     *            if tag identifiers should be treated as regular expression
     * @return the compiled pattern
     */
    private Pattern compile(final String tagIdentifiers, final CaseMode caseMode, final MatcherMode matcherMode) {
        try {
            if (matcherMode == MatcherMode.REGEXP_MATCH) {
                return Pattern.compile(tagIdentifiers); // use the tag as such
            }

            List<String> regexps = new ArrayList<String>();
            for (String tag : splitTags(tagIdentifiers)) {
                String trimmed = tag.trim();
                if (StringUtils.isNotBlank(trimmed)) {
                    StringBuilder actual = new StringBuilder();
                    if (Character.isLetterOrDigit(trimmed.charAt(0))) {
                        actual.append(WORD_BOUNDARY);
                    }
                    actual.append(trimmed);
                    if (Character.isLetterOrDigit(trimmed.charAt(trimmed.length() - 1))) {
                        actual.append(WORD_BOUNDARY);
                    }
                    regexps.add(actual.toString());
                }
            }

            return Pattern.compile("^.*(" + StringUtils.join(regexps.iterator(), "|") + ")(.*)$", 
                    mapCaseMode());
        }
        catch (PatternSyntaxException exception) {
            isInvalidPattern = true;
            errors.append(String.format("Specified pattern is an invalid regular expression: '%s': '%s'", 
                    tagIdentifiers, exception.getMessage()));

            return INVALID;
        }
    }

    private int mapCaseMode() {
        if (caseMode == CaseMode.IGNORE_CASE) {
            return Pattern.CASE_INSENSITIVE;
        }
        else {
            return 0;
        }
    }

    private String[] splitTags(final String tagIdentifiers) {
        if (tagIdentifiers.indexOf(',') == -1) {
            return new String[] {tagIdentifiers};
        }
        else {
            return StringUtils.split(tagIdentifiers, ",");
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
    public Report scan(final Reader reader) {
        Report report = new Report();
        if (isInvalidPattern) {
            report.logError(errors.toString());  
            return report;
        }
        
        try {
            LineIterator lineIterator = IOUtils.lineIterator(reader);

            IssueBuilder builder = new IssueBuilder();
            for (int lineNumber = 1; lineIterator.hasNext(); lineNumber++) {
                String line = lineIterator.next();

                for (Severity severity : Severity.getPredefinedValues()) {
                    if (patterns.containsKey(severity)) {
                        Matcher matcher = patterns.get(severity).matcher(line);
                        if (matcher.matches() && matcher.groupCount() == 2) {
                            String message = matcher.group(2).trim();
                            String tag = matcher.group(1);
                            if (caseMode == CaseMode.IGNORE_CASE) {
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
            try {
                reader.close();
            }
            catch (IOException ignore) {
                // ignore
            }
        }
    }
}

