package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import org.junit.platform.commons.util.StringUtils;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.TrendChartType;

/**
 * Page object for a configuration of the post build step "Record compiler warnings and static analysis results".
 *
 * @author Florian Hageneder
 * @author Ullrich Hafner
 */
@SuppressWarnings("JavaDocMethod")
public class FreestyleConfiguration extends PageObject {
    private static final String IGNORE_QUALITY_GATE = "_.ignoreQualityGate";
    private static final String IGNORE_FAILED_BUILDS = "_.ignoreFailedBuilds";
    private static final String REFERENCE_JOB_NAME = "_.referenceJobName";
    private static final String FAIL_ON_ERROR = "_.failOnError";

    private static final String HEALTHY = "_.healthy";
    private static final String UNHEALTHY = "_.unhealthy";
    private static final String MINIMUM_SEVERITY = "_.minimumSeverity";

    private static final String SOURCE_CODE_ENCODING = "_.sourceCodeEncoding";
    private static final String SOURCE_DIRECTORY = "_.sourceDirectory";

    private static final String PATTERN = "_.pattern";
    private static final String BLAME_DISABLED = "_.blameDisabled";
    private static final String FORENSICS_DISABLED = "_.forensicsDisabled";
    private static final String ENABLED_FOR_FAILURE = "_.enabledForFailure";
    private static final String AGGREGATING_RESULTS = "_.aggregatingResults";
    private static final String TREND_CHART_TYPE = "_.trendChartType";

    private final HtmlForm form;

    /**
     * Creates a page object for the given configuration page.
     *
     * @param page
     *         fetched configuration html page.
     */
    public FreestyleConfiguration(final HtmlPage page) {
        super(page);

        form = page.getFormByName("config");
    }

    HtmlForm getForm() {
        return form;
    }

    /**
     * Sets the encoding to use to read source files.
     *
     * @param sourceCodeEncoding
     *         the encoding, e.g. "ISO-8859-1"
     *
     * @return this
     */
    public FreestyleConfiguration setSourceCodeEncoding(final String sourceCodeEncoding) {
        setText(SOURCE_CODE_ENCODING, sourceCodeEncoding);

        return this;
    }

    public String getSourceCodeEncoding() {
        return getTextOf(SOURCE_CODE_ENCODING);
    }

    /**
     * Sets the path to the folder that contains the source code. If not relative and thus not part of the workspace
     * then this folder needs to be added in Jenkins global configuration.
     *
     * @param sourceDirectory
     *         a folder containing the source code
     *
     * @return this
     */
    public FreestyleConfiguration setSourceDirectory(final String sourceDirectory) {
        setText(SOURCE_DIRECTORY, sourceDirectory);

        return this;
    }

    public String getSourceDirectory() {
        return getTextOf(SOURCE_DIRECTORY);
    }

    /**
     * Determines whether the results for each configured static analysis result should be aggregated into a single
     * result or if every tool should get an individual result.
     *
     * @param aggregatingResults
     *         {@code true}  if the results of each static analysis tool should be aggregated into a single result,
     *         {@code false} if every tool should get an individual result.
     *
     * @return this
     */
    public FreestyleConfiguration setAggregatingResults(final boolean aggregatingResults) {
        setChecked(AGGREGATING_RESULTS, aggregatingResults);

        return this;
    }

    public boolean isAggregatingResults() {
        return isChecked(AGGREGATING_RESULTS);
    }

    /**
     * Determines whether SCM blaming is disabled.
     *
     * @param blameDisabled
     *         {@code true} if SCM blaming should be disabled, {@code false} otherwise
     *
     * @return this
     */
    public FreestyleConfiguration setBlameDisabled(final boolean blameDisabled) {
        setChecked(BLAME_DISABLED, blameDisabled);

        return this;
    }

    public boolean isBlameDisabled() {
        return isChecked(BLAME_DISABLED);
    }

    /**
     * Determines whether SCM forensics is disabled.
     *
     * @param forensicsDisabled
     *         {@code true} if SCM forensics should be disabled, {@code false} otherwise
     *
     * @return this
     */
    public FreestyleConfiguration setForensicsDisabled(final boolean forensicsDisabled) {
        setChecked(FORENSICS_DISABLED, forensicsDisabled);

        return this;
    }

    public boolean isForensicsDisabled() {
        return isChecked(FORENSICS_DISABLED);
    }

    /**
     * Determines whether to fail the build on errors during the step of recording issues.
     *
     * @param failOnError
     *         if {@code true} then the build will be failed on errors, {@code false} then errors are only reported in
     *         the UI
     *
     * @return this
     */
    public FreestyleConfiguration setFailOnError(final boolean failOnError) {
        setChecked(FAIL_ON_ERROR, failOnError);
        return this;
    }

    public boolean isFailOnErrorEnabled() {
        return isChecked(FAIL_ON_ERROR);
    }

    /**
     * Returns whether recording should be enabled for failed builds as well.
     *
     * @param enabledForFailure
     *         {@code true} if recording should be enabled for failed builds as well, {@code false} if recording is
     *         enabled for successful or unstable builds only
     *
     * @return this
     */
    public FreestyleConfiguration setEnabledForFailure(final boolean enabledForFailure) {
        setChecked(ENABLED_FOR_FAILURE, enabledForFailure);

        return this;
    }

    public boolean isEnabledForFailure() {
        return isChecked(ENABLED_FOR_FAILURE);
    }

    /**
     * If {@code true}, then the result of the quality gate is ignored when selecting a reference build. This option is
     * disabled by default so a failing quality gate will be passed from build to build until the original reason for
     * the failure has been resolved.
     *
     * @param ignoreQualityGate
     *         if {@code true} then the result of the quality gate is ignored, otherwise only build with a successful
     *         quality gate are selected
     *
     * @return this
     */
    public FreestyleConfiguration setIgnoreQualityGate(final boolean ignoreQualityGate) {
        setChecked(IGNORE_QUALITY_GATE, ignoreQualityGate);

        return this;
    }

    public boolean isIgnoreQualityGateEnabled() {
        return isChecked(IGNORE_QUALITY_GATE);
    }

    /**
     * If {@code true}, then only successful or unstable reference builds will be considered. This option is enabled by
     * default, since analysis results might be inaccurate if the build failed. If {@code false}, every build that
     * contains a static analysis result is considered, even if the build failed.
     *
     * @param ignoreFailedBuilds
     *         if {@code true} then a stable build is used as reference
     *
     * @return this
     */
    public FreestyleConfiguration setIgnoreFailedBuilds(final boolean ignoreFailedBuilds) {
        setChecked(IGNORE_FAILED_BUILDS, ignoreFailedBuilds);

        return this;
    }

    public boolean isIgnoreFailedBuildsEnabled() {
        return isChecked(IGNORE_FAILED_BUILDS);
    }

    /**
     * Sets the reference job to get the results for the issue difference computation.
     *
     * @param referenceJobName
     *         the name of reference job
     *
     * @return this
     */
    public FreestyleConfiguration setReferenceJobName(final String referenceJobName) {
        setText(REFERENCE_JOB_NAME, referenceJobName);

        return this;
    }

    /**
     * Returns the reference job to get the results for the issue difference computation.
     *
     * @return the name of reference job
     */
    public String getReferenceJobName() {
        return getTextOf(REFERENCE_JOB_NAME);
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
     *
     * @return this
     */
    public FreestyleConfiguration setPattern(final String pattern) {
        setText(PATTERN, pattern);

        return this;
    }

    /**
     * Sets the report file pattern for given configured tool.
     *
     * @param pattern
     *         Pattern to be set.
     * @param id
     *         Id of the Tool.
     *
     * @return this
     */
    public FreestyleConfiguration setPattern(final String pattern, final int id) {
        form.getInputsByName(PATTERN).get(id).setValueAttribute(pattern);
        return this;
    }

    /**
     * Sets the health report thresholds.
     *
     * @param healthy
     *         threshold for healthy builds
     * @param unhealthy
     *         threshold for unhealthy builds
     * @param minimumSeverity
     *         minimum severity to consider
     *
     * @return this
     */
    public FreestyleConfiguration setHealthReport(final int healthy, final int unhealthy,
            final Severity minimumSeverity) {
        setText(HEALTHY, Integer.toString(healthy));
        setText(UNHEALTHY, Integer.toString(unhealthy));
        select(MINIMUM_SEVERITY, minimumSeverity.getName());

        return this;
    }

    private void select(final String name, final String value) {
        HtmlSelect select = getForm().getSelectByName(name);
        select.setSelectedAttribute(select.getOptionByValue(value), true);
    }

    public String getHealthy() {
        return getTextOf(HEALTHY);
    }

    public String getUnhealthy() {
        return getTextOf(UNHEALTHY);
    }

    public Severity getMinimumSeverity() {
        return Severity.valueOf(getSelectValue(MINIMUM_SEVERITY));
    }

    private String getSelectValue(final String name) {
        HtmlSelect select = getForm().getSelectByName(name);
        HtmlOption selected = select.getSelectedOptions().get(0);

        String valueAttribute = selected.getValueAttribute();
        if (StringUtils.isBlank(valueAttribute)) {
            throw new IllegalArgumentException("No value set for " + name);
        }
        return valueAttribute;
    }

    public TrendChartType getTrendChartType() {
        return TrendChartType.valueOf(getSelectValue(TREND_CHART_TYPE));
    }

    /**
     * Sets the trend chart position.
     *
     * @param trendChartType
     *         the trend type
     *
     * @return this
     */
    public FreestyleConfiguration setTrendChartType(final TrendChartType trendChartType) {
        select(TREND_CHART_TYPE, trendChartType.name());

        return this;
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
        return getForm().getInputByName(name);
    }

    /**
     * Saves the configuration.
     */
    public void save() {
        try {
            HtmlFormUtil.submit(getForm());
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
