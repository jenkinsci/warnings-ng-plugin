package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.Optional;

import org.codehaus.groovy.control.CompilationFailedException;
import org.eclipse.jgit.util.io.AutoLFInputStream.IsBinaryException;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import groovy.lang.Script;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        IssueBuilder builder = new IssueBuilder();
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(TRUE_SCRIPT);
        assertThat(matcher.createIssue(null, builder, 0, FILE_NAME)).isEmpty();
    }

    @Test
    void shouldReturnFalsePositiveIfScriptIsNotValid() {
        IssueBuilder builder = new IssueBuilder();
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(ILLEGAL_PARSER_SCRIPT);
        assertThat(matcher.createIssue(null, builder, 0, FILE_NAME)).isEmpty();
    }

    private Script createMatcher(final String sourceCode) {
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(sourceCode);
        Script script = matcher.compile();
        assertThat(script).isNotNull();
        return script;
    }

    @Test
    void shouldThrowCompilationFailedExceptionIfGroovyScriptContainsErrors() {
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(ILLEGAL_PARSER_SCRIPT);

        assertThatThrownBy(matcher::compile).isInstanceOf(CompilationFailedException.class);
    }

    @Test
    void shouldThrow() {
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(EXCEPTION_PARSER_SCRIPT);

        assertThat(matcher.run(null, new IssueBuilder(), 0, FILE_NAME)).isEqualTo(Optional.empty());
    }

    @Test
    void shouldCreateIssueWithLineNumberAndFileName() {
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(
                "return builder.setLineStart(lineNumber).setFileName(fileName).buildOptional()");

        Object result = matcher.run(null, new IssueBuilder(), 15, FILE_NAME);
        assertThat(result).isEqualTo(new IssueBuilder().setLineStart(15).setFileName("File.txt").buildOptional());
    }
}