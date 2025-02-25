package io.jenkins.plugins.analysis.warnings.groovy;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.Issue;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.SerializableTest;

import java.io.IOException;
import java.io.StringReader;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.ConsoleLogReaderFactory;
import io.jenkins.plugins.analysis.warnings.groovy.GroovyParser.DescriptorImpl;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link GroovyParser}.
 *
 * @author Ullrich Hafner
 */
class GroovyParserTest extends SerializableTest<GroovyParser> {
    private static final String SINGLE_LINE_EXAMPLE = "file/name/relative/unix:42:evil: this is a warning message";
    private static final String MULTI_LINE_EXAMPLE
            = "    [javac] 1. WARNING in C:\\Desenvolvimento\\Java\\jfg\\src\\jfg\\AttributeException.java (at line 3)\n"
            + "    [javac]     public class AttributeException extends RuntimeException\n"
            + "    [javac]                  ^^^^^^^^^^^^^^^^^^\n"
            + "    [javac] The serializable class AttributeException does not declare a static final serialVersionUID field of type long\n"
            + "    [javac] ----------\n";
    private static final String MULTI_LINE_REGEXP = "(WARNING|ERROR)\\s*in\\s*(.*)\\(at line\\s*(\\d+)\\).*"
            + "(?:\\r?\\n[^\\^]*)+(?:\\r?\\n.*[\\^]+.*)\\r?\\n(?:\\s*\\[.*\\]\\s*)?(.*)";
    private static final String SINGLE_LINE_REGEXP = "^\\s*(.*):(\\d+):(.*):\\s*(.*)$";
    private static final String OK_SCRIPT = ";";

    @Override
    protected void assertThatRestoredInstanceEqualsOriginalInstance(
            final GroovyParser original, final GroovyParser restored) {
        assertThat(restored).isEqualTo(original);
    }

    @Test
    void shouldShortenExample() {
        char[] example = new char[GroovyParser.MAX_EXAMPLE_SIZE * 2];
        GroovyParser parser = createParser(SINGLE_LINE_REGEXP, OK_SCRIPT, new String(example));

        assertThat(parser.getExample()).hasSize(GroovyParser.MAX_EXAMPLE_SIZE);
    }

    /**
     * Tries to expose JENKINS-35262: multi-line regular expression parser.
     *
     * @param regexp
     *         the regular expression to check
     *
     * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-35262">Issue 35262</a>
     */
    @Issue("JENKINS-35262")
    @ParameterizedTest(name = "{index}: Regular expression should be multiline \"{0}\"")
    @ValueSource(strings = {"\\n|\\r\\n", "\\r", "\\R"})
    void issue35262(final String regexp) throws IOException {
        var multiLineRegexp = String.format("(make(?:(?!make)[\\s\\S])*?make-error:.*(?:%s?))", regexp);
        String textToMatch = toString("issue35262.log");
        String script = toString("issue35262.groovy");

        GroovyParser parser = createParser(multiLineRegexp, script);
        assertThat(parser.hasMultiLineSupport()).as("Wrong multi line support guess").isTrue();

        DescriptorImpl descriptor = createDescriptor();
        assertThat(descriptor.checkExample(textToMatch, multiLineRegexp, script)).isOk();

        IssueParser instance = parser.createParser();
        Run<?, ?> run = mock(Run.class);
        when(run.getLogReader()).thenReturn(new StringReader(textToMatch));
        Report warnings = instance.parse(new ConsoleLogReaderFactory(run));

        assertThat(warnings).hasSize(1);
    }

    private GroovyParser createParser(final String multiLineRegexp, final String script) {
        return createParser(multiLineRegexp, script, "example");
    }

    private GroovyParser createParser(final String multiLineRegexp, final String script, final String example) {
        return createParser(multiLineRegexp, script, example, "name");
    }

    private GroovyParser createParser(final String multiLineRegexp, final String script, final String example,
            final String name) {
        GroovyParser parser = new GroovyParser("id", name, multiLineRegexp, script, example);
        parser.setJenkinsFacade(createJenkinsFacade());
        return parser;
    }

    private GroovyParser createParser(final String multiLineRegexp) {
        return createParser(multiLineRegexp, OK_SCRIPT);
    }

    @Test @Issue("JENKINS-60154")
    void shouldThrowExceptionDueToBrokenId() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new GroovyParser("broken id", "name", MULTI_LINE_REGEXP, OK_SCRIPT, "example"));
    }

    @Test
    void shouldThrowExceptionDueToMissingName() {
        GroovyParser groovyParser = createParser(MULTI_LINE_REGEXP, OK_SCRIPT, "example", StringUtils.EMPTY);
        assertThat(groovyParser.isValid()).isFalse();
        assertThatIllegalArgumentException().isThrownBy(groovyParser::createParser)
                .withMessageContaining("Name is not valid");
    }

    @Test
    void shouldThrowExceptionDueToBrokenScript() {
        GroovyParser groovyParser = createParser(SINGLE_LINE_REGEXP, StringUtils.EMPTY);
        assertThat(groovyParser.isValid()).isFalse();
        assertThatIllegalArgumentException().isThrownBy(groovyParser::createParser)
                .withMessageContaining("Script is not valid");
    }

    @Test
    void shouldThrowExceptionDueToBrokenRegExp() {
        GroovyParser groovyParser = createParser("one brace (", OK_SCRIPT);
        assertThat(groovyParser.isValid()).isFalse();
        assertThatIllegalArgumentException().isThrownBy(groovyParser::createParser)
                .withMessageContaining("RegExp is not valid");
    }

    @Test
    void shouldDetectMultiLineRegularExpression() {
        GroovyParser parser = createParser(MULTI_LINE_REGEXP);
        assertThat(parser.isValid()).isTrue();

        assertThat(parser.hasMultiLineSupport()).as("Wrong multi line support guess").isTrue();
        assertThat(parser.createParser()).isInstanceOf(DynamicDocumentParser.class);
    }

    @Test
    void shouldDetectSingleLineRegularExpression() {
        GroovyParser parser = createParser(SINGLE_LINE_REGEXP);

        assertThat(parser.hasMultiLineSupport()).as("Wrong single line support guess").isFalse();
        assertThat(parser.createParser()).isInstanceOf(DynamicLineParser.class);
    }

    @Test
    void shouldAcceptOnlyNonEmptyStringsAsName() {
        DescriptorImpl descriptor = createDescriptor();

        assertThat(descriptor.checkName(null)).isError();
        assertThat(descriptor.checkName(StringUtils.EMPTY)).isError();
        assertThat(descriptor.checkName("Java Parser 2")).isOk();
    }

    @Test
    void shouldRejectInvalidRegularExpressions() {
        DescriptorImpl descriptor = createDescriptor();

        assertThat(descriptor.checkRegexp(null)).isError();
        assertThat(descriptor.checkRegexp(StringUtils.EMPTY)).isError();
        assertThat(descriptor.checkRegexp("one brace (")).isError();
        assertThat(descriptor.checkRegexp("backslash \\")).isError();

        assertThat(descriptor.checkRegexp("^.*[a-z]")).isOk();
    }

    @Test
    void shouldRejectInvalidScripts() {
        DescriptorImpl descriptor = createDescriptor();

        assertThat(descriptor.checkScript(null)).isError();
        assertThat(descriptor.checkScript(StringUtils.EMPTY)).isError();
        assertThat(descriptor.checkScript("Hello World")).isError();

        assertThat(descriptor.checkScript(toString("parser.groovy"))).isOk();
    }

    @Test
    void shouldFindOneIssueWithValidScriptAndRegularExpression() {
        DescriptorImpl descriptor = createDescriptor();

        assertThat(descriptor.checkExample(SINGLE_LINE_EXAMPLE, SINGLE_LINE_REGEXP,
                toString("parser.groovy"))).isOk();
    }

    @Test
    void shouldReportErrorWhenNoMatchesAreFoundInExample() {
        DescriptorImpl descriptor = createDescriptor();

        assertThat(descriptor.checkExample("this is a warning message", SINGLE_LINE_REGEXP,
                toString("parser.groovy"))).isError();
    }

    @Test
    void shouldReportErrorWhenRegularExpressionHasIllegalMatchAccess() {
        DescriptorImpl descriptor = createDescriptor();

        assertThat(descriptor.checkExample(SINGLE_LINE_EXAMPLE, "^\\s*(.*):(\\d+):(.*)$",
                toString("parser.groovy"))).isError();
    }

    @Test
    void shouldAcceptMultiLineRegularExpression() {
        DescriptorImpl descriptor = createDescriptor();

        assertThat(descriptor.checkExample(MULTI_LINE_EXAMPLE, MULTI_LINE_REGEXP,
                toString("multiline.groovy"))).isOk();
    }

    private DescriptorImpl createDescriptor() {
        return createDescriptor(createJenkinsFacade());
    }

    private JenkinsFacade createJenkinsFacade() {
        JenkinsFacade facade = mock(JenkinsFacade.class);
        when(facade.hasPermission(any())).thenReturn(true);
        return facade;
    }

    private DescriptorImpl createDescriptor(final JenkinsFacade facade) {
        return new DescriptorImpl(facade);
    }

    @Override
    protected GroovyParser createSerializable() {
        return createParser("regexp", "script", "example");
    }
}

