package io.jenkins.plugins.analysis.core.model;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.analysis.core.util.HtmlBuilder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Provides a model for the issues table.
 *
 * @author Ullrich Hafner
 */
public class IssuesTableModel {
    private final Function<Integer, String> ageBuilder;

    /**
     * Creates a new {@link IssuesTableModel}.
     *
     * @param currentBuild
     *         the current build number
     * @param baseUrl
     *         the URL of the static analysis results of the current build
     */
    public IssuesTableModel(final int currentBuild, final String baseUrl) {
        ageBuilder = new AgeBuilder(currentBuild, baseUrl);
    }

    @VisibleForTesting
    IssuesTableModel(final Function<Integer, String> ageBuilder) {
        this.ageBuilder = ageBuilder;
    }

    /**
     * Converts the specified set of issues into a table.
     *
     * @param issues
     *         the issues to show in the table
     *
     * @return the table as String
     */
    public JSONObject toJsonArray(final Issues<BuildIssue> issues) {
        JSONArray rows = new JSONArray();
        for (BuildIssue issue : issues) {
            rows.add(issue.toJson(ageBuilder));
        }
        JSONObject data = new JSONObject();
        data.put("data", rows);
        return data;
    }

    public String[] getHeaders() {
        return new String[] {
                Messages.Table_Column_File(),
                Messages.Table_Column_Package(),
                Messages.Table_Column_Category(),
                Messages.Table_Column_Type(),
                Messages.Table_Column_Priority(),
                Messages.Table_Column_Age()
        };
    }

    public int[] getWidths() {
        return new int[] {1, 2, 1, 1, 1, 1};
    }

    static class AgeBuilder implements Function<Integer, String> {
        private final String plugin;
        private final String backward;
        private final int currentBuild;

        AgeBuilder(final int currentBuild, final String resultUrl) {
            this.currentBuild = currentBuild;
            String cleanUrl = StringUtils.stripEnd(resultUrl, "/");
            plugin = StringUtils.substringBefore(cleanUrl, "/");
            int subDetailsCount = StringUtils.countMatches(cleanUrl, "/");

            backward = StringUtils.repeat("../", subDetailsCount + 2);
        }

        @Override
        public String apply(final Integer origin) {
            if (origin >= currentBuild) {
                return "1"; // fallback
            }
            else {
                return new HtmlBuilder().linkWithClass(String.format("%s%d/%s", backward, origin, plugin),
                        computeAge(origin), "model-link inside").build();
            }
        }

        private String computeAge(final int buildNumber) {
            return String.valueOf(currentBuild - buildNumber + 1);
        }
    }
}
