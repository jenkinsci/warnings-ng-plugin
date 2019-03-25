package io.jenkins.plugins.analysis.warnings.axivion;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks whether the {@link AxivionParser} can parse all six supported issue kinds and transform them to Jenkins {@link
 * Issue}'s.
 */
class AxivionParserTest {

    private AxivionParser parser = new AxivionParser("testUrl", "");
    private final TestDashboard dashboard = new TestDashboard();

    @Test
    void canParseStyleViolations() {
        Report report = new Report();
        parser.parse(report, AxIssueKind.SV, dashboard.getIssues(AxIssueKind.SV));
        Issue issue = report.get(0);
        assertAll(
                () -> assertEquals("MisraC++-7.1.1", issue.getType()),
                () -> assertEquals("/src/mainwindow.h", issue.getFileName()),
                () -> assertEquals("", issue.getOrigin()),
                () -> assertEquals("mainwindow.h", issue.getBaseName()),
                () -> assertEquals("SV", issue.getCategory()),
                () -> assertEquals("SV1", issue.getFingerprint()),
                () -> assertEquals("", issue.getReference()),
                () -> assertEquals("/src", issue.getFolder()),
                () -> assertEquals("-", issue.getPackageName()),
                () -> assertEquals("-", issue.getModuleName()),
                () -> assertEquals(
                        "A parameter which is not modified shall be const qualified. <i>parent</i><p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/SV1\">More details</a>",
                        issue.getDescription())
        );
    }

    @Test
    void canParseMetricViolations() {
        Report report = new Report();
        parser.parse(report, AxIssueKind.MV, dashboard.getIssues(AxIssueKind.MV));
        Issue issue = report.get(0);
        assertAll(
                () -> assertEquals("Maximum nesting", issue.getType()),
                () -> assertEquals("/src/mainwindow.cpp", issue.getFileName()),
                () -> assertEquals("", issue.getOrigin()),
                () -> assertEquals("mainwindow.cpp", issue.getBaseName()),
                () -> assertEquals("MV", issue.getCategory()),
                () -> assertEquals("MV55", issue.getFingerprint()),
                () -> assertEquals("", issue.getReference()),
                () -> assertEquals("/src", issue.getFolder()),
                () -> assertEquals("-", issue.getPackageName()),
                () -> assertEquals("-", issue.getModuleName()),
                () -> assertEquals(
                        "Method <i>operator()</i><p>Val: <b>0</b><br>Max: 5<br>Min: 1<p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/MV55\">More details</a>",
                        issue.getDescription())
        );
    }

    @Test
    void canParseDeadEntities() {
        Report report = new Report();
        parser.parse(report, AxIssueKind.DE, dashboard.getIssues(AxIssueKind.DE));
        Issue issue = report.get(0);
        assertAll(
                () -> assertEquals("Dead Entity", issue.getType()),
                () -> assertEquals("/src/pointmodel.cpp", issue.getFileName()),
                () -> assertEquals("", issue.getOrigin()),
                () -> assertEquals("pointmodel.cpp", issue.getBaseName()),
                () -> assertEquals("DE", issue.getCategory()),
                () -> assertEquals("DE7", issue.getFingerprint()),
                () -> assertEquals("", issue.getReference()),
                () -> assertEquals("/src", issue.getFolder()),
                () -> assertEquals("-", issue.getPackageName()),
                () -> assertEquals("-", issue.getModuleName()),
                () -> assertEquals(
                        "Method<i>rowCount</i><p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/DE7\">More details</a>",
                        issue.getDescription())
        );
    }

    @Test
    void canParseArchitectureViolations() {
        Report report = new Report();
        parser.parse(report, AxIssueKind.AV, dashboard.getIssues(AxIssueKind.AV));
        Issue issue = report.get(0);
        assertAll(
                () -> assertEquals("Divergence", issue.getType()),
                () -> assertEquals("/projects/tools/gravis2/src/mainwindow.cpp", issue.getFileName()),
                () -> assertEquals("", issue.getOrigin()),
                () -> assertEquals("mainwindow.cpp", issue.getBaseName()),
                () -> assertEquals("AV", issue.getCategory()),
                () -> assertEquals("AV26941", issue.getFingerprint()),
                () -> assertEquals("", issue.getReference()),
                () -> assertEquals("/projects/tools/gravis2/src", issue.getFolder()),
                () -> assertEquals("-", issue.getPackageName()),
                () -> assertEquals("-", issue.getModuleName()),
                () -> assertEquals(
                        "Unexpected dependency from <i>Cluster &lt;gravis2:main&gt;</i> to <i>Cluster &lt;gravis2:dg model&gt;</i><p>Cause is a <i>Static_Call</i> dependency from <i>Method &lt;open_file_dialog&gt;</i> to <i>Method &lt;open_file_dialog&gt;</i><p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/AV26941\">More details</a>",
                        issue.getDescription())
        );
    }

    @Test
    void canParseClones() {
        Report report = new Report();
        parser.parse(report, AxIssueKind.CL, dashboard.getIssues(AxIssueKind.CL));
        Issue issue = report.get(0);
        assertAll(
                () -> assertEquals("type 2", issue.getType()),
                () -> assertEquals("/projects/plugins/dg_scripting/generated/dg.cpp", issue.getFileName()),
                () -> assertEquals("", issue.getOrigin()),
                () -> assertEquals("dg.cpp", issue.getBaseName()),
                () -> assertEquals("CL", issue.getCategory()),
                () -> assertEquals("CL476033", issue.getFingerprint()),
                () -> assertEquals("", issue.getReference()),
                () -> assertEquals("/projects/plugins/dg_scripting/generated", issue.getFolder()),
                () -> assertEquals("-", issue.getPackageName()),
                () -> assertEquals("-", issue.getModuleName()),
                () -> assertEquals(
                        "Left part of clone pair of type 2 clone of length 54LOC<p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/CL476033\">More details</a>",
                        issue.getDescription())
        );
    }

    @Test
    void canParseCycles() {
        Report report = new Report();
        parser.parse(report, AxIssueKind.CY, dashboard.getIssues(AxIssueKind.CY));
        Issue issue = report.get(0);
        assertAll(
                () -> assertEquals("Cycle", issue.getType()),
                () -> assertEquals("/usr/include/c++/4.9/bits/codecvt.h", issue.getFileName()),
                () -> assertEquals("", issue.getOrigin()),
                () -> assertEquals("codecvt.h", issue.getBaseName()),
                () -> assertEquals("CY", issue.getCategory()),
                () -> assertEquals("CY1471", issue.getFingerprint()),
                () -> assertEquals("", issue.getReference()),
                () -> assertEquals("/usr/include/c++/4.9/bits", issue.getFolder()),
                () -> assertEquals("-", issue.getPackageName()),
                () -> assertEquals("-", issue.getModuleName()),
                () -> assertEquals(
                        "Source: codecvt Target: __codecvt_abstract_base<p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/CY1471\">More details</a>",
                        issue.getDescription())
        );
    }

    @Test
    void canParseMultipleViolationInRows() {
        Report report = new Report();
        String resourcePath = "/io/jenkins/plugins/analysis/warnings/axivion/multiple-violations.json";
        parser.parse(report, AxIssueKind.SV, dashboard.getIssuesFrom(resourcePath));
        assertEquals(3, report.size());
    }
}
