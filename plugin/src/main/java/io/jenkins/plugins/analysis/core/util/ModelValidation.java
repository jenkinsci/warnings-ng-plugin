package io.jenkins.plugins.analysis.core.util;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.VisibleForTesting;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.regex.Pattern;

import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Validates all properties of a configuration of a static analysis tool in a job.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.GodClass")
public class ModelValidation {
    private static final Set<String> ALL_CHARSETS = Charset.availableCharsets().keySet();
    private static final Pattern VALID_ID_PATTERN = Pattern.compile("\\p{Alnum}[\\p{Alnum}-_.]*");

    @VisibleForTesting
    static final String NO_REFERENCE_JOB = "-";

    private final JenkinsFacade jenkins;

    /** Creates a new descriptor. */
    public ModelValidation() {
        this(new JenkinsFacade());
    }

    @VisibleForTesting
    ModelValidation(final JenkinsFacade jenkins) {
        super();

        this.jenkins = jenkins;
    }

    static String createInvalidIdMessage(final String id) {
        return Messages.FieldValidator_Error_WrongIdFormat(VALID_ID_PATTERN.pattern(), id);
    }

    /**
     * Returns a model with all available severity filters.
     *
     * @return a model with all available severity filters
     */
    public ListBoxModel getAllSeverityFilters() {
        var options = new ListBoxModel();
        options.add(Messages.SeverityFilter_Error(), Severity.ERROR.getName());
        options.add(Messages.SeverityFilter_High(), Severity.WARNING_HIGH.getName());
        options.add(Messages.SeverityFilter_Normal(), Severity.WARNING_NORMAL.getName());
        options.add(Messages.SeverityFilter_Low(), Severity.WARNING_LOW.getName());
        return options;
    }

    /**
     * Returns a model with all available aggregation trend chart positions.
     *
     * @return a model with all available aggregation trend chart positions
     */
    public ListBoxModel getAllTrendChartTypes() {
        var options = new ListBoxModel();
        options.add(Messages.AggregationChart_AGGREGATION_TOOLS(), TrendChartType.AGGREGATION_TOOLS.name());
        options.add(Messages.AggregationChart_TOOLS_AGGREGATION(), TrendChartType.TOOLS_AGGREGATION.name());
        options.add(Messages.AggregationChart_TOOLS_ONLY(), TrendChartType.TOOLS_ONLY.name());
        options.add(Messages.AggregationChart_AGGREGATION_ONLY(), TrendChartType.AGGREGATION_ONLY.name());
        options.add(Messages.AggregationChart_NONE(), TrendChartType.NONE.name());
        return options;
    }

    /**
     * Returns the model with the possible reference jobs.
     *
     * @return the model with the possible reference jobs
     */
    public ComboBoxModel getAllJobs() {
        var model = new ComboBoxModel(jenkins.getAllJobNames());
        model.add(0, NO_REFERENCE_JOB); // make sure that no input is valid
        return model;
    }

    /**
     * Performs on-the-fly validation of the reference job.
     *
     * @param referenceJobName
     *         the reference job
     *
     * @return the validation result
     */
    public FormValidation validateJob(final String referenceJobName) {
        if (NO_REFERENCE_JOB.equals(referenceJobName)
                || StringUtils.isBlank(referenceJobName)
                || jenkins.getJob(referenceJobName).isPresent()) {
            return FormValidation.ok();
        }
        return FormValidation.error(Messages.FieldValidator_Error_ReferenceJobDoesNotExist());
    }

    /**
     * Performs on-the-fly validation of the quality gate threshold.
     *
     * @param threshold
     *         the threshold
     *
     * @return the validation result
     */
    public FormValidation validateThreshold(final int threshold) {
        if (threshold > 0) {
            return FormValidation.ok();
        }
        return FormValidation.error(Messages.FieldValidator_Error_NegativeThreshold());
    }

    /**
     * Performs on-the-fly validation of the health report thresholds.
     *
     * @param healthy
     *         the healthy threshold
     * @param unhealthy
     *         the unhealthy threshold
     *
     * @return the validation result
     */
    public FormValidation validateHealthy(final int healthy, final int unhealthy) {
        if (healthy > 0 && unhealthy <= 0) {
            return FormValidation.ok();
        }
        return validateHealthReportConstraints(healthy, healthy, unhealthy);
    }

    /**
     * Performs on-the-fly validation of the health report thresholds.
     *
     * @param healthy
     *         the healthy threshold
     * @param unhealthy
     *         the unhealthy threshold
     *
     * @return the validation result
     */
    public FormValidation validateUnhealthy(final int healthy, final int unhealthy) {
        if (healthy < 0 && unhealthy == 0) {
            return FormValidation.ok();
        }
        if (healthy > 0 && unhealthy == 0) {
            return FormValidation.error(Messages.FieldValidator_Error_ThresholdUnhealthyMissing());
        }
        return validateHealthReportConstraints(unhealthy, healthy, unhealthy);
    }

    private FormValidation validateHealthReportConstraints(final int positive,
            final int healthy, final int unhealthy) {
        if (healthy == 0 && unhealthy == 0) {
            return FormValidation.ok();
        }
        if (positive <= 0) {
            return FormValidation.error(Messages.FieldValidator_Error_NegativeThreshold());
        }
        if (healthy >= unhealthy) {
            if (unhealthy <= 0) {
                return FormValidation.error(Messages.FieldValidator_Error_NegativeThreshold());
            }
            return FormValidation.error(Messages.FieldValidator_Error_ThresholdOrder());
        }
        return FormValidation.ok();
    }
}
