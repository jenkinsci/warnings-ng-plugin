package io.jenkins.plugins.analysis.warnings.axivion;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import net.sf.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

class AxivionParserTest {

    @Test
    void parseAxivionIssuesViaDashboardJsonReport() {
        JSONObject payload = new TestDashboard().getIssues(AxIssueKind.SV);
        AxivionParser parser = new AxivionParser("testUrl", "testDir");

        Report report = new Report();
        parser.parse(report, AxIssueKind.SV, payload);

        String[] expected = {
                "MisraC++-7.1.1",
                "Generic-NamingConvention",
                "Style.Indentation-AllmanBraces"
        };

        for (final Issue actual : report) {
            assertTrue(ArrayUtils.contains(expected, actual.getType()));
        }
    }
}


