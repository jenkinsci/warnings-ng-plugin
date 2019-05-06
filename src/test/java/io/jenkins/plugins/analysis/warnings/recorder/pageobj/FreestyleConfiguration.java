package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page object for a configuration of the post build step "Record compiler warnings and static analysis results".
 *
 * @author Florian Hageneder
 */
public class FreestyleConfiguration {
    private static final String HEALTHY = "_.healthy";
    private static final String UNHEALTHY = "_.unhealthy";
    private static final String SOURCE_CODE_ENCODING = "_.sourceCodeEncoding";
    private static final String PATTERN = "_.pattern";
    private static final String BLAME_DISABLED = "_.blameDisabled";
    private static final String AGGREGATING_RESULTS = "_.aggregatingResults";

    private final HtmlForm form;

    /**
     * Creates a page object for the given configuration page.
     *
     * @param page
     *         fetched configuration html page.
     */
    public FreestyleConfiguration(final HtmlPage page) {
        form = page.getFormByName("config");
    }

    public String getSourceCodeEncoding() {
        return getTextOf(SOURCE_CODE_ENCODING);
    }

    /**
     * Sets the encoding to use to read source files.
     *
     * @param sourceCodeEncoding
     *         the encoding, e.g. "ISO-8859-1"
     */
    public void setSourceCodeEncoding(final String sourceCodeEncoding) {
        setText(SOURCE_CODE_ENCODING, sourceCodeEncoding);
    }

    /**
     * Returns whether the results for each configured static analysis result should be aggregated into a single result
     * or if every tool should get an individual result.
     *
     * @return {@code true}  if the results of each static analysis tool should be aggregated into a single result,
     *         {@code false} if every tool should get an individual result.
     */
    public boolean isAggregatingResults() {
        return isChecked(AGGREGATING_RESULTS);
    }

    public void setAggregatingResults(final boolean aggregatingResults) {
        setChecked(AGGREGATING_RESULTS, aggregatingResults);
    }

    /**
     * Returns whether SCM blaming is disabled.
     *
     * @return {@code true} if SCM blaming is disabled
     */
    public boolean isBlameDisabled() {
        return isChecked(BLAME_DISABLED);
    }

    public void setDisableBlame(final boolean checked) {
        setChecked(BLAME_DISABLED, checked);
    }

    /**
     * Returns the report file pattern of the first configured tool.
     *
     * @return the pattern
     */
    public String getPattern() {
        return getTextOf(PATTERN);
    }

    /**
     * Sets the report file pattern for the first configured tool.
     *
     * @param pattern
     *         Pattern to be set.
     */
    public void setPattern(final String pattern) {
        setText(PATTERN, pattern);
    }

    /**
     * Sets the health report thresholds.
     *
     * @param healthy
     *         threshold for healthy builds
     * @param unhealthy
     *         threshold for unstable builds
     */
    public void setHealthReport(final int healthy, final int unhealthy) {
        setText(HEALTHY, Integer.toString(healthy));
        setText(UNHEALTHY, Integer.toString(unhealthy));
    }

    public String getHealthy() {
        return getNumberAsString(HEALTHY);
    }

    public String getUnhealthy() {
        return getNumberAsString(UNHEALTHY);
    }

    private String getNumberAsString(final String s) {
        return getTextOf(s);
    }

    private String getTextOf(final String id) {
        return getInputByName(id).getValueAttribute();
    }

    private void setText(final String id, final String value) {
        getInputByName(id).setValueAttribute(value);
    }

    private boolean isChecked(final String aggregatingResults) {
        return getInputByName(aggregatingResults).isChecked();
    }

    private void setChecked(final String id, final boolean value) {
        getInputByName(id).setChecked(value);
    }

    private HtmlInput getInputByName(final String name) {
        return form.getInputByName(name);
    }

    /**
     * Saves the configuration.
     */
    public void save() {
        try {
            HtmlFormUtil.submit(form);
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
