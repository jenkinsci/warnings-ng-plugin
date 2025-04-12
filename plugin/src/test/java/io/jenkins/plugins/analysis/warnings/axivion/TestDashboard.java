package io.jenkins.plugins.analysis.warnings.axivion;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;

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
        final var testCase = this.getClass().getResource(resourcePath);
        return JsonParser.parseReader(new Resource(testCase).asReader()).getAsJsonObject();
    }

    private String resolveResourcePath(final AxIssueKind kind) {
        var resource = "/io/jenkins/plugins/analysis/warnings/axivion/";
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

    /**
     * Wraps a resource found by {@link Class#getResource(String)}.
     *
     * @author Kohsuke Kawaguchi
     */
    @SuppressWarnings("all")
    @SuppressFBWarnings({"DM", "URLCONNECTION_SSRF_FD"})
    public static class Resource {
        public final URL url;

        public Resource(URL url) {
            this.url = url;
        }

        /**
         * Gets just the file name portion without any paths, like "foo.txt"
         */
        public String getName() {
            var s = url.toExternalForm();
            return s.substring(s.lastIndexOf('/') + 1);
        }

        public InputStream asInputStream() {
            try {
                return url.openStream();
            } catch (IOException e) {
                throw new IOError(e);
            }
        }

        public Reader asReader() {
            try {
                return new InputStreamReader(url.openStream());
            } catch (IOException e) {
                throw new IOError(e);
            }
        }

        public byte[] asByteArray() throws IOException {
            try (var is = asInputStream()) {
                return IOUtils.toByteArray(is);
            }
        }

        public File asFile() {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOError(e);
            }
        }
    }
}
