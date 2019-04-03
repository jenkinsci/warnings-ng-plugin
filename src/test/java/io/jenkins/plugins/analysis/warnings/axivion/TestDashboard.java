package io.jenkins.plugins.analysis.warnings.axivion;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

import net.sf.json.JSONObject;

import io.jenkins.plugins.analysis.warnings.Resource;

/**
 * Stub for an {@link AxivionDashboard} to retrieve actual violations from the resources folder instead of actually
 * opening a connection to a real dashboard.
 */
class TestDashboard implements AxivionDashboard {

    @Override
    public JSONObject getIssues(final AxIssueKind kind) {
        return getIssuesFrom(resolveResourcePath(kind));
    }

    JSONObject getIssuesFrom(final String resourcePath) {
        URL svJson = this.getClass().getResource(resourcePath);
        try {
            final String payload = new String(new Resource(svJson).asByteArray());
            return JSONObject.fromObject(payload);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String resolveResourcePath(final AxIssueKind kind) {
        String resource = "/io/jenkins/plugins/analysis/warnings/axivion/";
        switch (kind) {
            case AV:
                resource += "av.json";
                break;
            case CL:
                resource += "cl.json";
                break;
            case CY:
                resource += "cy.json";
                break;
            case DE:
                resource += "de.json";
                break;
            case MV:
                resource += "mv.json";
                break;
            case SV:
                resource += "sv.json";
                break;
        }
        return resource;
    }
}
