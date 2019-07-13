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
import edu.umd.cs.findbugs.annotations.Nullable;

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

    private static final String PATTERN = "_.pattern";
    private static final String BLAME_DISABLED = "_.blameDisabled";
    private static final String ENABLED_FOR_FAILURE = "_.enabledForFailure";
    private static final String AGGREGATING_RESULTS = "_.aggregatingResults";

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
     *         {@code true} if SCM blaming should be disabled, {@code false} if blames should be collected in the SCM
     *
     * @return this
     */
    public FreestyleConfiguration setBlameDisabled(final boolean blameDisabled) {
        setChecked(BLAME_DISABLED, blameDisabled);

        return this;
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

    public boolean mustFailOnError() {
        return isChecked(FAIL_ON_ERROR);
    }

    public boolean isBlameDisabled() {
        return isChecked(BLAME_DISABLED);
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

    public boolean canIgnoreQualityGate() {
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

    public boolean canIgnoreFailedBuilds() {
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

        HtmlSelect select = getForm().getSelectByName(MINIMUM_SEVERITY);
        select.setSelectedAttribute(select.getOptionByValue(minimumSeverity.getName()), true);

        return this;
    }

    public String getHealthy() {
        return getTextOf(HEALTHY);
    }

    public String getUnhealthy() {
        return getTextOf(UNHEALTHY);
    }

    @Nullable
    public Severity getMinimumSeverity() {
        HtmlSelect select = getForm().getSelectByName(MINIMUM_SEVERITY);
        HtmlOption selected = select.getSelectedOptions().get(0);

        String valueAttribute = selected.getValueAttribute();
        if (StringUtils.isBlank(valueAttribute)) {
            return null;
        }
        return Severity.valueOf(valueAttribute);
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
