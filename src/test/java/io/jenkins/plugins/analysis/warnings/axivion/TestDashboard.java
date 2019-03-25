package io.jenkins.plugins.analysis.warnings.axivion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

import net.sf.json.JSONObject;

import io.jenkins.plugins.analysis.warnings.Resource;

public class TestDashboard implements AxivionDashboard {

    @Override
    public JSONObject getIssues(final AxIssueKind kind) {
        URL svJson = this.getClass()
                .getResource("/io/jenkins/plugins/analysis/warnings/axivion/sv.json");
        try {
            final String payload = new String(new Resource(svJson).asByteArray());
            return JSONObject.fromObject(payload);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
