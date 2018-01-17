package io.jenkins.plugins.analysis.warnings.groovy;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import groovy.lang.Script;
import static org.assertj.core.api.Assertions.*;

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

    @Test
    void shouldCreateScriptIfSourceCodeIsValid() {
        assertThat(createMatcher(TRUE_SCRIPT).run()).isEqualTo(true);
        assertThat(createMatcher(FALSE_SCRIPT).run()).isEqualTo(false);
    }

    @Test
    void shouldReturnFalsePositiveIfWrongObjectTypeIsReturned() {
        IssueBuilder builder = new IssueBuilder();
        Issue falsePositive = builder.build();
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(TRUE_SCRIPT, falsePositive);
        assertThat(matcher.createIssue(null, builder, 0)).isSameAs(falsePositive);
    }

    @Test
    void shouldReturnFalsePositiveIfScriptIsNotValid() {
        IssueBuilder builder = new IssueBuilder();
        Issue falsePositive = builder.build();
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(ILLEGAL_PARSER_SCRIPT, falsePositive);
        assertThat(matcher.createIssue(null, builder, 0)).isSameAs(falsePositive);
    }

    private Script createMatcher(final String sourceCode) {
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(sourceCode, null);
        Script script = matcher.compile();
        assertThat(script).isNotNull();
        return script;
    }

    @Test
    void shouldThrowCompilationFailedExceptionIfGroovyScriptContainsErrors() {
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(ILLEGAL_PARSER_SCRIPT, null);

        assertThatThrownBy(() -> matcher.compile()).isInstanceOf(CompilationFailedException.class);
    }

    @Test
    void shouldThrow() {
        Issue falsePositive = new IssueBuilder().build();
        GroovyExpressionMatcher matcher = new GroovyExpressionMatcher(EXCEPTION_PARSER_SCRIPT, falsePositive);

        assertThat(matcher.run(null, new IssueBuilder(), 0)).isSameAs(falsePositive);
    }
}