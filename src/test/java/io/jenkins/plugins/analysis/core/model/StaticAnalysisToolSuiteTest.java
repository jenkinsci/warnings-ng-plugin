package io.jenkins.plugins.analysis.core.model;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class StaticAnalysisToolSuiteTest {

    private Issue issue = new IssueBuilder().setMessage("test")
            .setFileName("filename")
            .setPriority(Priority.HIGH)
            .build();
    private File file = mock(File.class);
    private Function<String, String> func = mock(Function.class);
    private Charset charset = mock(Charset.class);
    private AbstractParser<Issue> parser = mock(AbstractParser.class);

    private StaticAnalysisToolSuite suite = new StaticAnalysisToolSuite() {
        @Override
        protected Collection<? extends AbstractParser<Issue>> getParsers() {
            Issues<Issue> list = new Issues<>();
            list.add(issue);
            when(parser.parse(file, charset, func)).thenReturn(list);
            return asList(parser);
        }
    };

    //parse
    @Test
    void shouldBeOkIfCreateDoesNotReturnNull() {
        IssueParser<Issue> parser = suite.createParser();
        assertThat(parser).isNotNull();
    }

    @Test
    void shouldBeOkIfAsListDoesNotReturnNull() {
        Collection<? extends AbstractParser<Issue>> result = suite.asList();
        assertThat(result).isNotNull();
    }

    @Test
    void shouldBeOkIfParseIsCalledExactlyOnce() {
        suite.createParser().parse(file, charset, func);
        verify(parser, times(1)).parse(file, charset, func);
    }

}