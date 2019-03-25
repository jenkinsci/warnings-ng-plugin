package io.jenkins.plugins.analysis.warnings.axivion;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import edu.hm.hafner.analysis.ParsingException;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Represents an actual dashboard connection to retrieve violations via http.
 */
class RemoteAxivionDashboard implements AxivionDashboard {

    private static final String X_AXIVION_USER_AGENT = "x-axivion-user-agent";
    private static final String API_USER_AGENT = "ApiClient/6.9.3";
    private static final String FALL_USER_AGENT = "AxivionEclipsePlugin/6.9.2";

    private final String projectUrl;
    private final UsernamePasswordCredentials credentials;

    RemoteAxivionDashboard(
            final String projectUrl,
            final UsernamePasswordCredentials credentials) {
        this.projectUrl = projectUrl;
        this.credentials = credentials;
    }

    @Override
    public JSONObject getIssues(final AxIssueKind kind) {
        final org.apache.http.client.CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();

        credentialsProvider.setCredentials(AuthScope.ANY, credentials);

        try (CloseableHttpClient client =
                     HttpClients.custom()
                             .setDefaultCredentialsProvider(credentialsProvider)
                             .build()) {

            HttpGet httpget = new HttpGet(this.projectUrl + "/issues?kind=" + kind);
            httpget.addHeader(new BasicHeader("Accept", "application/json"));
            BasicHeader userAgent = new BasicHeader(X_AXIVION_USER_AGENT, API_USER_AGENT);
            httpget.addHeader(userAgent);

            try (CloseableHttpResponse response = client.execute(httpget)) {
                if (response.getStatusLine().getStatusCode() == 200) {
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
            throw new ParsingException("Cannot retrieve information from dashboard." + e.getMessage());
        }
        catch (JSONException e) {
            throw new ParsingException("Invalid JSON response from dashboard." + e.getMessage());
        }
    }

    private JSONObject convertToJson(final HttpResponse response) throws IOException {
        final String result = EntityUtils.toString(response.getEntity());
        return JSONObject.fromObject(result);
    }
}
