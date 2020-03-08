package io.jenkins.plugins.analysis.core.model;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.ResourceTest;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ReportScanningToolSuite}.
 *
 * @author Arne Schöntag
 */
class StaticAnalysisToolSuiteTest extends ResourceTest {
    private static final ReaderFactory FILE = mock(ReaderFactory.class);

    @Test
    void shouldReturnEmptyReportIfSuiteIsEmpty() {
        TestReportScanningToolSuite suite = new TestReportScanningToolSuite();

        Report issues = suite.createParser().parse(FILE);

        assertThat(issues).isEmpty();
    }

    @Test
    void shouldReturnReportOfSingleParser() {
        IssueParser parser = createParserStub();
        Report issues = createIssues(1);
        when(parser.parse(FILE)).thenReturn(issues);

        TestReportScanningToolSuite suite = new TestReportScanningToolSuite(parser);

        Report compositeIssues = suite.createParser().parse(FILE);

        assertThat(compositeIssues).isEqualTo(issues);
    }

    @Test
    void shouldReturnAggregationOfTwoParsers() {
        IssueParser firstParser = createParserStub();
        Report issuesFirstParser = createIssues(1);
        when(firstParser.parse(FILE)).thenReturn(issuesFirstParser);

        IssueParser secondParser = createParserStub();
        Report issuesSecondParser = createIssues(2);
        when(secondParser.parse(FILE)).thenReturn(issuesSecondParser);

        TestReportScanningToolSuite suite = new TestReportScanningToolSuite(firstParser, secondParser);

        Report compositeIssues = suite.createParser().parse(FILE);

        Report expected = new Report();
        expected.addAll(issuesFirstParser, issuesSecondParser);
        assertThat(compositeIssues).isEqualTo(expected);
    }

    private IssueParser createParserStub() {
        IssueParser stub = mock(IssueParser.class);
        
        when(stub.accepts(any())).thenReturn(true);
        
        return stub;
    }

    private Report createIssues(final int id) {
        Report issues = new Report();
        
        IssueBuilder issueBuilder = new IssueBuilder();
        issues.add(issueBuilder.setMessage(String.valueOf(id)).build());
        
        return issues;
    }

    /**
     * {@link ReportScanningToolSuite} to be used in the tests.
     */
    private class TestReportScanningToolSuite extends ReportScanningToolSuite {
        private static final long serialVersionUID = -1564903699146113905L;
        private final Collection<? extends IssueParser> parsers;

        TestReportScanningToolSuite(final IssueParser... parsers) {
            super();

            this.parsers = asList(parsers);
        }

        @Override
        protected Collection<? extends IssueParser> getParsers() {
            return parsers;
        }
    }
}