package io.jenkins.plugins.analysis.warnings.axivion;

import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.jenkins.plugins.analysis.warnings.Resource;

/**
 * Stub for an {@link AxivionDashboard} to retrieve actual violations from the resources folder instead of actually
 * opening a connection to a real dashboard.
 */
class TestDashboard implements AxivionDashboard {

    @Override
    public JsonObject getIssues(final AxIssueKind kind) {
        return getIssuesFrom(resolveResourcePath(kind));
    }

    JsonObject getIssuesFrom(final String resourcePath) {
        final URL testCase = this.getClass().getResource(resourcePath);
        return new JsonParser().parse(new Resource(testCase).asReader()).getAsJsonObject();
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
