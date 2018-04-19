package io.jenkins.plugins.analysis.core.model;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link StaticAnalysisToolSuite}.
 *
 * @author Arne Schöntag
 */
class StaticAnalysisToolSuiteTest {

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
    void shouldBeOkIfAsListDoesContainParser() {
        List<AbstractParser<Issue>> result = (List<AbstractParser<Issue>>) suite.asList(parser);
        assertThat(result).contains(this.parser);
    }

    @Test
    void shouldBeOkIfParseIsPerformedCorrectlyWithOneParser() {
        Issues<Issue> issues = suite.createParser().parse(file, charset, func);
        verify(parser, times(1)).parse(file, charset, func);
        assertThat(issues.get(0)).isEqualTo(issue);
    }

    @Test
    void shouldBeOkIfParseIsPerformedCorrectlyWithMoreParsers() {
        AbstractParser<Issue> parser2 = mock(AbstractParser.class);
        Issue issue2 = new IssueBuilder().setMessage("second")
                .setFileName("filename2")
                .setPriority(Priority.LOW)
                .build();

        AbstractParser<Issue> parser3 = mock(AbstractParser.class);
        Issue issue3 = new IssueBuilder().setMessage("third")
                .setFileName("filename3")
                .setPriority(Priority.NORMAL)
                .build();

        StaticAnalysisToolSuite toolSuite = new StaticAnalysisToolSuite() {
            @Override
            protected Collection<? extends AbstractParser<Issue>> getParsers() {
                Issues<Issue> list = new Issues<>();
                list.add(issue);
                when(parser.parse(file, charset, func)).thenReturn(list);

                Issues<Issue> list2 = new Issues<>();
                list.add(issue2);
                when(parser2.parse(file, charset, func)).thenReturn(list2);

                Issues<Issue> list3 = new Issues<>();
                list.add(issue3);
                when(parser3.parse(file, charset, func)).thenReturn(list3);
                return asList(parser, parser2, parser3);
            }
        };
        Issues<Issue> issues = toolSuite.createParser().parse(file, charset, func);
        assertThat(issues).hasSize(3);
    }

}