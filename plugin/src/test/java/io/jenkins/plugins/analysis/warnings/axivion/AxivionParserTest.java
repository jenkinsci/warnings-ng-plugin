package io.jenkins.plugins.analysis.warnings.axivion;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Report;

import io.jenkins.plugins.analysis.warnings.axivion.AxivionParser.Config;

import static org.assertj.core.api.Assertions.*;

/**
 * Checks whether the {@link AxivionParser} can parse all six supported issue kinds and transform them to warnings.
 */
class AxivionParserTest {
    private final AxivionParser parser = new AxivionParser(new Config("testUrl", "/root", true));
    private final TestDashboard dashboard = new TestDashboard();

    @Test
    void canParseStyleViolations() {
        var report = new Report();

        parser.parse(report, AxIssueKind.SV, dashboard.getIssues(AxIssueKind.SV));
        var issue = report.get(0);

        assertThat(issue.getType()).isEqualTo("MisraC++-7.1.1");
        assertThat(issue.getFileName()).isEqualTo("src/mainwindow.h");
        assertThat(issue.getOrigin()).isEmpty();
        assertThat(issue.getBaseName()).isEqualTo("mainwindow.h");
        assertThat(issue.getCategory()).isEqualTo("SV");
        assertThat(issue.getFingerprint()).isEqualTo("SV1");
        assertThat(issue.getReference()).isEmpty();
        assertThat(issue.getFolder()).isEqualTo("src");
        assertThat(issue.getPackageName()).isEqualTo("-");
        assertThat(issue.getModuleName()).isEqualTo("-");
        assertThat(issue.getDescription()).isEqualTo(
                "A parameter which is not modified shall be const qualified. <i>parent</i><p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/SV1\">More details</a>");
    }

    @Test
    void canParseMetricViolations() {
        var report = new Report();

        parser.parse(report, AxIssueKind.MV, dashboard.getIssues(AxIssueKind.MV));
        var issue = report.get(0);

        assertThat(issue.getType()).isEqualTo("Maximum nesting");
        assertThat(issue.getFileName()).isEqualTo("src/mainwindow.cpp");
        assertThat(issue.getOrigin()).isEmpty();
        assertThat(issue.getBaseName()).isEqualTo("mainwindow.cpp");
        assertThat(issue.getCategory()).isEqualTo("MV");
        assertThat(issue.getFingerprint()).isEqualTo("MV55");
        assertThat(issue.getReference()).isEmpty();
        assertThat(issue.getFolder()).isEqualTo("src");
        assertThat(issue.getPackageName()).isEqualTo("-");
        assertThat(issue.getModuleName()).isEqualTo("-");
        assertThat(issue.getDescription()).isEqualTo(
                "Method <i>operator()</i><p>Val: <b>0</b><br>Max: 5<br>Min: 1<p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/MV55\">More details</a>");
    }

    @Test
    void canParseDeadEntities() {
        var report = new Report();

        parser.parse(report, AxIssueKind.DE, dashboard.getIssues(AxIssueKind.DE));
        var issue = report.get(0);

        assertThat(issue.getType()).isEqualTo("Dead Entity");
        assertThat(issue.getFileName()).isEqualTo("src/pointmodel.cpp");
        assertThat(issue.getOrigin()).isEmpty();
        assertThat(issue.getBaseName()).isEqualTo("pointmodel.cpp");
        assertThat(issue.getCategory()).isEqualTo("DE");
        assertThat(issue.getFingerprint()).isEqualTo("DE7");
        assertThat(issue.getReference()).isEmpty();
        assertThat(issue.getFolder()).isEqualTo("src");
        assertThat(issue.getPackageName()).isEqualTo("-");
        assertThat(issue.getModuleName()).isEqualTo("-");
        assertThat(issue.getDescription()).isEqualTo(
                "Method<i>rowCount</i><p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/DE7\">More details</a>");
    }

    @Test
    void canParseArchitectureViolations() {
        var report = new Report();

        parser.parse(report, AxIssueKind.AV, dashboard.getIssues(AxIssueKind.AV));
        var issue = report.get(0);

        assertThat(issue.getType()).isEqualTo("Divergence");
        assertThat(issue.getFileName()).isEqualTo("projects/tools/gravis2/src/mainwindow.cpp");
        assertThat(issue.getOrigin()).isEmpty();
        assertThat(issue.getBaseName()).isEqualTo("mainwindow.cpp");
        assertThat(issue.getCategory()).isEqualTo("AV");
        assertThat(issue.getFingerprint()).isEqualTo("AV26941");
        assertThat(issue.getReference()).isEmpty();
        assertThat(issue.getFolder()).isEqualTo("projects/tools/gravis2/src");
        assertThat(issue.getPackageName()).isEqualTo("-");
        assertThat(issue.getModuleName()).isEqualTo("-");
        assertThat(issue.getDescription()).isEqualTo(
                "Unexpected dependency from <i>Cluster &lt;gravis2:main&gt;</i> to <i>Cluster &lt;gravis2:dg model&gt;</i><p>Cause is a <i>Static_Call</i> dependency from <i>Method &lt;open_file_dialog&gt;</i> to <i>Method &lt;get_basepath&gt;</i><p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/AV26941\">More details</a>");
    }

    @Test
    void canParseClones() {
        var report = new Report();

        parser.parse(report, AxIssueKind.CL, dashboard.getIssues(AxIssueKind.CL));
        var issue = report.get(0);

        assertThat(issue.getType()).isEqualTo("type 2");
        assertThat(issue.getFileName()).isEqualTo("projects/plugins/dg_scripting/generated/dg.cpp");
        assertThat(issue.getOrigin()).isEmpty();
        assertThat(issue.getBaseName()).isEqualTo("dg.cpp");
        assertThat(issue.getCategory()).isEqualTo("CL");
        assertThat(issue.getFingerprint()).isEqualTo("CL476033");
        assertThat(issue.getReference()).isEmpty();
        assertThat(issue.getFolder()).isEqualTo("projects/plugins/dg_scripting/generated");
        assertThat(issue.getPackageName()).isEqualTo("-");
        assertThat(issue.getModuleName()).isEqualTo("-");
        assertThat(issue.getDescription()).isEqualTo(
                "Left part of clone pair of type 2 clone of length 54LOC<p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/CL476033\">More details</a>");
    }

    @Test
    void canParseCycles() {
        var report = new Report();

        parser.parse(report, AxIssueKind.CY, dashboard.getIssues(AxIssueKind.CY));
        var issue = report.get(0);

        assertThat(issue.getType()).isEqualTo("Cycle");
        assertThat(issue.getFileName()).isEqualTo("/usr/include/c++/4.9/bits/codecvt.h");
        assertThat(issue.getOrigin()).isEmpty();
        assertThat(issue.getBaseName()).isEqualTo("codecvt.h");
        assertThat(issue.getCategory()).isEqualTo("CY");
        assertThat(issue.getFingerprint()).isEqualTo("CY1471");
        assertThat(issue.getReference()).isEmpty();
        assertThat(issue.getFolder()).isEqualTo("usr/include/c++/4.9/bits");
        assertThat(issue.getPackageName()).isEqualTo("-");
        assertThat(issue.getModuleName()).isEqualTo("-");
        assertThat(issue.getDescription()).isEqualTo(
                "Source: codecvt Target: __codecvt_abstract_base<p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\"testUrl/issues/CY1471\">More details</a>");
    }

    @Test
    void canParseMultipleViolationInRows() {
        var report = new Report();
        parser.parse(report, AxIssueKind.SV, dashboard.getIssuesFrom(
                "/io/jenkins/plugins/analysis/warnings/axivion/multiple-violations.json"));
        assertThat(report).hasSize(3);
    }

    @Test
    void absenceTypeArchitectureViolationsMayNotHaveAPath() {
        var report = new Report();

        parser.parse(report, AxIssueKind.AV,
                dashboard.getIssuesFrom("/io/jenkins/plugins/analysis/warnings/axivion/av_empty_paths.json"));
        var issue = report.get(0);

        assertThat(issue.getType()).isEqualTo("Absence");
        assertThat(issue.getFileName()).isEqualTo("-");
    }

    @Test
    void parserIsAwareOfDashboardErrors() {
        var report = new Report();

        parser.parse(report, AxIssueKind.SV, dashboard.getIssuesFrom(
                "/io/jenkins/plugins/analysis/warnings/axivion/dashboard_error.json"));

        assertThat(report.hasErrors()).isTrue();
        assertThat(report.getSize()).isZero();
    }

    @Test
    void parserIgnoresSuppressedOrJustifiedIssues() {
        var report = new Report();

        parser.parse(report, AxIssueKind.SV, dashboard.getIssuesFrom(
                "/io/jenkins/plugins/analysis/warnings/axivion/sv_justified_and_suppressed.json"));

        assertThat(report.hasErrors()).isFalse();
        assertThat(report.getSize()).isZero();
    }
}
