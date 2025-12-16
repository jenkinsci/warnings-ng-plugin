package io.jenkins.plugins.analysis.warnings.groovy;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;

import groovy.lang.Script;
import java.util.Optional;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

/**
 * Tests the class {@link GroovyExpressionMatcher}.
 *
 * @author Ullrich Hafner
 */
class GroovyExpressionMatcherTest {
    private static final String TRUE_SCRIPT = "return Boolean.TRUE";
    private static final String FALSE_SCRIPT = "return Boolean.FALSE";
    private static final String EXCEPTION_PARSER_SCRIPT = "throw new IllegalArgumentException()";
    private static final String ILLEGAL_PARSER_SCRIPT = "0:0";
    private static final String FILE_NAME = "File.txt";

    @Test
    void shouldCreateScriptIfSourceCodeIsValid() {
        assertThat(createMatcher(TRUE_SCRIPT).run()).isEqualTo(true);
        assertThat(createMatcher(FALSE_SCRIPT).run()).isEqualTo(false);
    }

    @Test
    void shouldReturnFalsePositiveIfWrongObjectTypeIsReturned() {
        try (var builder = new IssueBuilder()) {
            var matcher = new GroovyExpressionMatcher(TRUE_SCRIPT);
            assertThat(matcher.createIssue(null, builder, 0, FILE_NAME)).isEmpty();
        }
    }

    @Test
    void shouldReturnFalsePositiveIfScriptIsNotValid() {
        try (var builder = new IssueBuilder()) {
            var matcher = new GroovyExpressionMatcher(ILLEGAL_PARSER_SCRIPT);
            assertThat(matcher.createIssue(null, builder, 0, FILE_NAME)).isEmpty();
        }
    }

    private Script createMatcher(final String sourceCode) {
        var matcher = new GroovyExpressionMatcher(sourceCode);
        var script = matcher.compile();
        assertThat(script).isNotNull();
        return script;
    }

    @Test
    void shouldThrowCompilationFailedExceptionIfGroovyScriptContainsErrors() {
        var matcher = new GroovyExpressionMatcher(ILLEGAL_PARSER_SCRIPT);

        assertThatThrownBy(matcher::compile).isInstanceOf(CompilationFailedException.class);
    }

    @Test
    void shouldThrow() {
        var matcher = new GroovyExpressionMatcher(EXCEPTION_PARSER_SCRIPT);

        assertThat(matcher.run(null, new IssueBuilder(), 0, FILE_NAME)).isEqualTo(Optional.empty());
    }

    @Test
    void shouldCreateIssueWithLineNumberAndFileName() {
        var matcher = new GroovyExpressionMatcher(
                "return builder.setLineStart(lineNumber).setFileName(fileName).buildOptional()");

        var result = matcher.run(null, new IssueBuilder(), 15, FILE_NAME);
        assertThat(result).isEqualTo(new IssueBuilder().setLineStart(15).setFileName("File.txt").buildOptional());
    }

    @Test
    void shouldAutomaticallySetFileNameAndLineStartWhenNotSetByScript() {
        // JENKINS-74818: Test that fileName and lineStart are automatically set when not explicitly set by the script
        var matcher = new GroovyExpressionMatcher(
                "return builder.setMessage('test message').buildOptional()");

        var result = matcher.run(null, new IssueBuilder(), 42, "test.txt");
        var expected = new IssueBuilder()
                .setFileName("test.txt")
                .setLineStart(42)
                .setMessage("test message")
                .buildOptional();
        assertThat(result).isEqualTo(expected);
    }
}
