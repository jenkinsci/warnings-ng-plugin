package io.jenkins.plugins.analysis.warnings.axivion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.hm.hafner.analysis.ParsingException;

/**
 * Represents an actual dashboard connection to retrieve violations via http.
 */
class RemoteAxivionDashboard implements AxivionDashboard {
    private static final String X_AXIVION_USER_AGENT = "x-axivion-user-agent";
    private static final String API_USER_AGENT = "ApiClient/6.9.3";
    private static final String FALL_USER_AGENT = "AxivionEclipsePlugin/6.9.2";
    private static final int HTTP_STATUS_OK = 200;

    private final String projectUrl;
    private final UsernamePasswordCredentials credentials;

    RemoteAxivionDashboard(final String projectUrl, final UsernamePasswordCredentials credentials) {
        this.projectUrl = projectUrl;
        this.credentials = credentials;
    }

    @Override
    public JsonObject getIssues(final AxIssueKind kind) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);

        try (CloseableHttpClient client =
                     HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build()) {

            HttpGet httpget = new HttpGet(projectUrl + "/issues?kind=" + kind);
            httpget.addHeader(new BasicHeader("Accept", "application/json"));
            BasicHeader userAgent = new BasicHeader(X_AXIVION_USER_AGENT, API_USER_AGENT);
            httpget.addHeader(userAgent);

            try (CloseableHttpResponse response = client.execute(httpget)) {
                if (response.getStatusLine().getStatusCode() == HTTP_STATUS_OK) {
                    return convertToJson(response);
                }
            }

            // dashboard version < 6.9.3 need the fallback header
            httpget.removeHeader(userAgent);
            httpget.addHeader(new BasicHeader(X_AXIVION_USER_AGENT, FALL_USER_AGENT));
            try (CloseableHttpResponse legacyResponse = client.execute(httpget)) {
                return convertToJson(legacyResponse);
            }
        }
        catch (IOException e) {
            throw new ParsingException(e, "Cannot retrieve information from dashboard");
        }
    }

    private JsonObject convertToJson(final HttpResponse response) throws IOException {
        try (InputStream is = response.getEntity().getContent()) {
            if (is == null) {
                throw new ParsingException("Response without a json body");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                final JsonElement json = new JsonParser().parse(reader);
                if (!json.isJsonObject()) {
                    throw new ParsingException("Invalid response from dashboard. Json object expected.");
                }
                return json.getAsJsonObject();
            }
        }
    }
}
