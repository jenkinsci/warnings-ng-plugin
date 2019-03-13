package io.jenkins.plugins.analysis.warnings.tasks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.umd.cs.findbugs.annotations.Nullable;

import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;

/**
 * Scans a given input stream for open tasks.
 *
 * @author Ullrich Hafner
 */
class TaskScanner {
    private static final String WORD_BOUNDARY = "\\b";
    private static final Pattern INVALID = Pattern.compile("");

    /** The regular expression patterns to be used to scan the files. One pattern per priority. */
    private final Map<Severity, Pattern> patterns = new HashMap<>();
    private final boolean isUppercase;

    private boolean isPatternInvalid;
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private final StringBuilder errors = new StringBuilder();

    /** Determines whether the tags are case sensitive or not. */
    public enum CaseMode {
        /** Tags are not case sensitive. */
        IGNORE_CASE,
        /** Tags are case sensitive. */
        CASE_SENSITIVE
    }

    /** Determines whether tags are plain strings or regular expressions. */
    public enum MatcherMode {
        /** Tags are interpreted as plain string. */
        STRING_MATCH,
        /** Tags are interpreted as regular expression. */
        REGEXP_MATCH
    }

    /**
     * Creates a new instance of {@link TaskScanner}.
     *
     * @param highTags
     *         tag identifiers indicating tasks with high severity
     * @param normalTags
     *         tag identifiers indicating tasks with normal severity
     * @param lowTags
     *         tag identifiers indicating low priority
     * @param caseMode
     *         if case should be ignored during matching
     * @param matcherMode
     *         if tag identifiers should be treated as regular expression
     */
    TaskScanner(final @Nullable String highTags, final @Nullable String normalTags,
            final @Nullable String lowTags,
            final CaseMode caseMode, final MatcherMode matcherMode) {
        this.isUppercase = caseMode == CaseMode.IGNORE_CASE;
        if (StringUtils.isNotBlank(highTags)) {
            patterns.put(Severity.WARNING_HIGH, compile(highTags, caseMode, matcherMode));
        }
        if (StringUtils.isNotBlank(normalTags)) {
            patterns.put(Severity.WARNING_NORMAL, compile(normalTags, caseMode, matcherMode));
        }
        if (StringUtils.isNotBlank(lowTags)) {
            patterns.put(Severity.WARNING_LOW, compile(lowTags, caseMode, matcherMode));
        }
    }

    String getTaskTags() {
        if (isPatternInvalid) {
            return "Invalid patterns detected:\n" + getErrors();
        }
        else if (patterns.isEmpty()) {
            return "No task tags have been defined. Configuration Error?\n";
        }
        else {
            StringBuilder builder = new StringBuilder("Using the following tasks patterns:\n");
            for (Severity severity : Severity.getPredefinedValues()) {
                if (patterns.containsKey(severity)) {
                    builder.append(String.format("-> %s : %s%n", LocalizedSeverity.getLocalizedString(severity),
                            patterns.get(severity)));
                }
            }
            return builder.toString();
        }
    }

    /**
     * Returns whether one of the tag patterns is invalid.
     *
     * @return {@code true} if one of the tag patterns is invalid, {@code false} if everything is fine
     */
    boolean isInvalidPattern() {
        return isPatternInvalid;
    }

    /**
     * Returns all error messages that have been reported during the pattern evaluation.
     *
     * @return the error messages
     */
    public String getErrors() {
        return errors.toString();
    }

    /**
     * Compiles a regular expression pattern to scan for tag identifiers.
     *
     * @param tagIdentifiers
     *         the identifiers to scan for
     * @param caseMode
     *         specifies if case should be ignored
     * @param matcherMode
     *         if tag identifiers should be treated as regular expression
     *
     * @return the compiled pattern
     */
    private Pattern compile(final String tagIdentifiers, final CaseMode caseMode, final MatcherMode matcherMode) {
        try {
            if (matcherMode == MatcherMode.REGEXP_MATCH) {
                return Pattern.compile(tagIdentifiers); // use the tag as such
            }

            List<String> regexps = new ArrayList<>();
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

            String regex = "^.*(" + StringUtils.join(regexps.iterator(), "|") + ")(.*)$";
            if (caseMode == CaseMode.IGNORE_CASE) {
                return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            }
            else {
                return Pattern.compile(regex);
            }
        }
        catch (PatternSyntaxException exception) {
            isPatternInvalid = true;
            errors.append(String.format("Specified pattern is an invalid regular expression: '%s': '%s'",
                    tagIdentifiers, exception.getMessage()));

            return INVALID;
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
     * Scans the specified file for open tasks.
     *
     * @param file
     *         the file to scan
     * @param charset
     *         encoding of the file
     *
     * @return the open tasks
     */
    public Report scan(final Path file, final Charset charset) {
        try (Stream<String> lines = Files.lines(file, charset)) {
            return scanTasks(lines.iterator(), new IssueBuilder().setFileName(file.toString()));
        }
        catch (IOException | UncheckedIOException exception) {
            Report report = new Report();
            Throwable cause = exception.getCause();
            if (cause instanceof MalformedInputException || cause instanceof UnmappableCharacterException) {
                report.logError("Can't read source file '%s', defined encoding '%s' seems to be wrong",
                        file, charset);
            }
            else {
                report.logException(exception, "Exception while reading the source code file '%s':", file);
            }

            return report;
        }
    }

    /**
     * Scans the specified source code lines for open tasks.
     *
     * @param lines
     *         the source code lines
     * @param builder
     *         the builder to create issue instances
     *
     * @return the open tasks
     */
    Report scanTasks(final Iterator<String> lines, final IssueBuilder builder) {
        Report report = new Report();

        if (isPatternInvalid) {
            report.logError("%s", errors.toString());
            return report;
        }

        for (int lineNumber = 1; lines.hasNext(); lineNumber++) {
            String line = lines.next();

            for (Severity severity : Severity.getPredefinedValues()) {
                if (patterns.containsKey(severity)) {
                    Matcher matcher = patterns.get(severity).matcher(line);
                    if (matcher.matches() && matcher.groupCount() == 2) {
                        String message = matcher.group(2).trim();
                        builder.setMessage(StringUtils.removeStart(message, ":").trim());

                        String tag = matcher.group(1);
                        if (isUppercase) {
                            builder.setType(StringUtils.upperCase(tag));
                        }
                        else {
                            builder.setType(tag);
                        }
                        report.add(builder.setSeverity(severity).setLineStart(lineNumber).build());
                    }
                }
            }
        }
        return report;
    }
}

