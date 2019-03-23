package io.jenkins.plugins.analysis.warnings.axivion;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import net.sf.json.JSONObject;

import io.jenkins.plugins.analysis.warnings.Resource;

import static org.junit.jupiter.api.Assertions.*;

class AxivionParserTest {

    @Test
    void parseAxivionIssuesViaDashboardJsonReport() throws IOException {
        AxivionParser parser = new AxivionParser("testUrl", null, "testDir");
        URL svJson = this.getClass().getResource("/io/jenkins/plugins/analysis/warnings/axivion/sv.json");
        final String payload = new String(new Resource(svJson).asByteArray());

        Report report = new Report();
        parser.processIssues(
                report,
                AxIssueKind.SV,
                JSONObject.fromObject(payload),
                AxIssueTransformation.SV
        );

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


