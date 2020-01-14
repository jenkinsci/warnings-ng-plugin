package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Validates all properties of a configuration of a static analysis tool in a job.
 *
 * @author Ullrich Hafner
 */
public class ModelValidation {
    private static final Set<String> ALL_CHARSETS = Charset.availableCharsets().keySet();
    private static final Pattern VALID_ID_PATTERN = Pattern.compile("\\p{Alnum}[\\p{Alnum}-_]*");

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

    /**
     * Returns all available character set names.
     *
     * @return all available character set names
     */
    public ComboBoxModel getAllCharsets() {
        return new ComboBoxModel(ALL_CHARSETS);
    }

    /**
     * Returns the default charset for the specified encoding string. If the default encoding is empty or {@code null},
     * or if the charset is not valid then the default encoding of the platform is returned.
     *
     * @param charset
     *         identifier of the character set
     *
     * @return the default charset for the specified encoding string
     */
    public Charset getCharset(@Nullable final String charset) {
        try {
            if (StringUtils.isNotBlank(charset)) {
                return Charset.forName(charset);
            }
        }
        catch (UnsupportedCharsetException | IllegalCharsetNameException exception) {
            // ignore and return default
        }
        return Charset.defaultCharset();
    }

    /**
     * Ensures that the specified ID is valid.
     *
     * @param id
     *         the custom ID of the tool
     *
     * @throws IllegalArgumentException if the ID is not valid
     */
    public void ensureValidId(final String id) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException(String.format("An ID must be a valid URL, but '%s' is not.", id));
        }
    }

    /**
     * Performs on-the-fly validation of the ID.
     *
     * @param id
     *         the custom ID of the tool
     *
     * @return the validation result
     */
    public FormValidation validateId(final String id) {
        if (isValidId(id)) {
            return FormValidation.ok();
        }
        return FormValidation.error(Messages.FieldValidator_Error_WrongIdFormat());
    }

    private boolean isValidId(final String id) {
        return StringUtils.isEmpty(id) || VALID_ID_PATTERN.matcher(id).matches();
    }

    /**
     * Performs on-the-fly validation of the character encoding.
     *
     * @param reportEncoding
     *         the character encoding
     *
     * @return the validation result
     */
    public FormValidation validateCharset(final String reportEncoding) {
        try {
            if (StringUtils.isBlank(reportEncoding) || Charset.isSupported(reportEncoding)) {
                return FormValidation.ok();
            }
        }
        catch (IllegalCharsetNameException | UnsupportedCharsetException ignore) {
            // throw a FormValidation error
        }
        return FormValidation.errorWithMarkup(createWrongEncodingErrorMessage());
    }

    /**
     * Returns a model with all available severity filters.
     *
     * @return a model with all available severity filters
     */
    public ListBoxModel getAllSeverityFilters() {
        ListBoxModel options = new ListBoxModel();
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
        ListBoxModel options = new ListBoxModel();
        options.add(Messages.AggregationChart_AGGREGATION_TOOLS(), TrendChartType.AGGREGATION_TOOLS.name());
        options.add(Messages.AggregationChart_TOOLS_AGGREGATION(), TrendChartType.TOOLS_AGGREGATION.name());
        options.add(Messages.AggregationChart_TOOLS_ONLY(), TrendChartType.TOOLS_ONLY.name());
        options.add(Messages.AggregationChart_NONE(), TrendChartType.NONE.name());
        return options;
    }

    /**
     * Returns the model with the possible reference jobs.
     *
     * @return the model with the possible reference jobs
     */
    public ComboBoxModel getAllJobs() {
        ComboBoxModel model = new ComboBoxModel(jenkins.getAllJobNames());
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

    @VisibleForTesting
    static String createWrongEncodingErrorMessage() {
        return Messages.FieldValidator_Error_DefaultEncoding(
                "https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html");
    }

    /**
     * Performs on-the-fly validation of the quality gate threshold.
     *
     * @param threshold
     *         the threshold
     *
     * @return the validation result
     */
    @SuppressWarnings("WeakerAccess")
    public FormValidation validateThreshold(@QueryParameter final int threshold) {
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

    /**
     * Performs on-the-fly validation on the ant pattern for input files.
     *
     * @param project
     *         the project
     * @param pattern
     *         the file pattern
     *
     * @return the validation result
     */
    public FormValidation doCheckPattern(@AncestorInPath final AbstractProject<?, ?> project,
            @QueryParameter final String pattern) {
        if (project != null) { // there is no workspace in pipelines
            try {
                FilePath workspace = project.getSomeWorkspace();
                if (workspace != null && workspace.exists()) {
                    return validatePatternInWorkspace(pattern, workspace);
                }
            }
            catch (InterruptedException | IOException ignore) {
                // ignore and return ok
            }
        }

        return FormValidation.ok();
    }

    private FormValidation validatePatternInWorkspace(final @QueryParameter String pattern,
            final FilePath workspace) throws IOException, InterruptedException {
        String result = workspace.validateAntFileMask(pattern, FilePath.VALIDATE_ANT_FILE_MASK_BOUND);
        if (result != null) {
            return FormValidation.error(result);
        }
        return FormValidation.ok();
    }

    /**
     * Performs on-the-fly validation on the source code directory.
     *
     * @param project
     *         the project
     * @param sourceDirectory
     *         the file pattern
     *
     * @return the validation result
     */
    public FormValidation doCheckSourceDirectory(@AncestorInPath final AbstractProject<?, ?> project,
            @QueryParameter final String sourceDirectory) {
        if (project != null) { // there is no workspace in pipelines
            try {
                FilePath workspace = project.getSomeWorkspace();
                if (workspace != null && workspace.exists()) {
                    return validateRelativePath(sourceDirectory, workspace);
                }
            }
            catch (InterruptedException | IOException ignore) {
                // ignore and return ok
            }
        }

        return FormValidation.ok();
    }

    private FormValidation validateRelativePath(
            @QueryParameter final String sourceDirectory, final FilePath workspace) throws IOException {
        PathUtil pathUtil = new PathUtil();
        if (pathUtil.isAbsolute(sourceDirectory)) {
            return FormValidation.ok();
        }
        return workspace.validateRelativeDirectory(sourceDirectory);
    }
}
