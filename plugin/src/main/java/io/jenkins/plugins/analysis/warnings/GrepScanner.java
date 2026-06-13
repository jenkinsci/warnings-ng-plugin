package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

/**
 * Scans files line by line for occurrences of a configurable regular expression and reports each match as an issue.
 *
 * <p>
 * This scanner is the core logic for the {@link GrepParser} tool. It is designed to be simple to use: users
 * supply a regular expression, an optional message template, and a severity. Each line of a file that matches the
 * expression is turned into a {@link edu.hm.hafner.analysis.Issue}.
 * </p>
 *
 * @author Akash Manna
 */
class GrepScanner {
    /** Sentinel pattern used when the user-supplied pattern is invalid. */
    private static final Pattern INVALID = Pattern.compile("");

    private final Pattern pattern;
    private final Severity severity;
    private final String messageTemplate;

    private boolean isPatternInvalid;
    private String errorMessage = "";

    /**
     * Creates a new {@link GrepScanner} instance.
     *
     * @param regexp
     *         the regular expression to match against each line
     * @param severity
     *         the severity to assign to matched issues
     * @param messageTemplate
     *         an optional message for matched issues; if blank the matched line content is used
     */
    GrepScanner(final String regexp, final Severity severity, final String messageTemplate) {
        this.severity = severity;
        this.messageTemplate = messageTemplate;
        this.pattern = compile(regexp);
    }

    private Pattern compile(final String regexp) {
        try {
            return Pattern.compile(regexp);
        }
        catch (PatternSyntaxException exception) {
            isPatternInvalid = true;
            errorMessage = "Specified pattern is an invalid regular expression: '%s': '%s'"
                    .formatted(regexp, exception.getMessage());
            return INVALID;
        }
    }

    /**
     * Returns whether the configured pattern is invalid.
     *
     * @return {@code true} if the pattern is invalid and scanning will produce no results
     */
    boolean isInvalidPattern() {
        return isPatternInvalid;
    }

    /**
     * Returns the error message produced during pattern compilation (if the pattern is invalid).
     *
     * @return the error message, or an empty string if the pattern is valid
     */
    String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Scans the specified file for lines that match the configured pattern.
     *
     * @param file
     *         the file to scan
     * @param charset
     *         the encoding to use when reading the file
     *
     * @return a report containing one issue per matching line
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            justification = "https://github.com/spotbugs/spotbugs/issues/756")
    Report scan(final Path file, final Charset charset) {
        try (Stream<String> lines = Files.lines(file, charset);
                IssueBuilder issueBuilder = new IssueBuilder()) {
            return scanLines(lines.iterator(), issueBuilder.setFileName(file.toString()));
        }
        catch (IOException | UncheckedIOException exception) {
            var report = new Report();
            var cause = exception.getCause();
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
     * Scans the specified lines for occurrences of the configured pattern.
     *
     * @param lines
     *         an iterator over the lines to scan
     * @param issueBuilder
     *         the builder used to create issue instances
     *
     * @return a report containing one issue per matching line
     */
    Report scanLines(final Iterator<String> lines, final IssueBuilder issueBuilder) {
        var report = new Report();

        if (isPatternInvalid) {
            report.logError("%s", errorMessage);
            return report;
        }

        for (int lineNumber = 1; lines.hasNext(); lineNumber++) {
            var line = lines.next();
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                var message = determineMessage(line, matcher);
                report.add(issueBuilder
                        .setSeverity(severity)
                        .setLineStart(lineNumber)
                        .setMessage(message)
                        .build());
            }
        }
        return report;
    }

    private String determineMessage(final String line, final Matcher matcher) {
        if (messageTemplate == null || messageTemplate.isBlank()) {
            return line.trim();
        }
        // Support $0 for whole match, $1 for group 1, etc.
        try {
            return matcher.replaceFirst(messageTemplate);
        }
        catch (IndexOutOfBoundsException | IllegalArgumentException ignored) {
            return line.trim();
        }
    }
}
