package io.jenkins.plugins.analysis.core.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.BuildableItem;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;
import io.jenkins.plugins.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult.*;

/**
 * Defines a quality gate based on a specific threshold of issues (total, new, delta) in the current build. After a
 * build has been finished, a set of {@link QualityGate quality gates} will be evaluated and the overall quality gate
 * status will be reported in Jenkins UI.
 *
 * @author Ullrich Hafner
 */
public class QualityGate extends AbstractDescribableImpl<QualityGate> implements Serializable {
    private static final long serialVersionUID = -397278599489416668L;

    private final int threshold;
    private final QualityGateType type;
    private final QualityGateStatus status;

    /**
     * Creates a new instance of {@link QualityGate}.
     *
     * @param threshold
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param unstable
     *         determines whether the build result will be set to unstable or failed if the quality gate is failed
     */
    @DataBoundConstructor
    public QualityGate(final int threshold, final QualityGateType type, final boolean unstable) {
        super();

        this.threshold = threshold;
        this.type = type;
        status = unstable ? QualityGateStatus.WARNING : QualityGateStatus.FAILED;
    }

    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getUnstable() {
        return status == QualityGateStatus.WARNING;
    }

    public QualityGateType getType() {
        return type;
    }

    @SuppressWarnings("PMD.BooleanGetMethodName")
    public boolean getWarning() {
        return status == QualityGateStatus.WARNING;
    }

    /**
     * Creates a new instance of {@link QualityGate}.
     *
     * @param threshold
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param result
     *         determines whether the quality gate is a warning or failure
     */
    public QualityGate(final int threshold, final QualityGateType type, final QualityGateResult result) {
        super();

        this.threshold = threshold;
        this.type = type;
        status = result.status;
    }

    /**
     * Returns the minimum number of issues that will fail the quality gate.
     *
     * @return minimum number of issues
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Returns the method that should be used to determine the actual number of issues in the build.
     *
     * @return threshold getter
     */
    public Function<IssuesStatistics, Integer> getActualSizeMethodReference() {
        return type.getSizeGetter();
    }

    /**
     * Returns the human-readable name of the quality gate.
     *
     * @return the human-readable name
     */
    public String getName() {
        return type.getDisplayName();
    }

    /**
     * Returns the quality gate status to set if the quality gate is failed.
     *
     * @return the status
     */
    public QualityGateStatus getStatus() {
        return status;
    }

    /**
     * Returns the quality gate status to set if the quality gate is failed.
     *
     * @return the status
     */
    public QualityGateResult getResult() {
        return status == QualityGateStatus.WARNING ? UNSTABLE : FAILURE;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QualityGate that = (QualityGate) o;
        return threshold == that.threshold && type == that.type && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(threshold, type, status);
    }

    /**
     * Determines the Jenkins build result if the quality gate is failed.
     */
    public enum QualityGateResult {
        /** The build will be marked as unstable. */
        UNSTABLE(QualityGateStatus.WARNING),

        /** The build will be marked as failed. */
        FAILURE(QualityGateStatus.FAILED);

        private final QualityGateStatus status;

        QualityGateResult(final QualityGateStatus status) {
            this.status = status;
        }

        /**
         * Returns the status.
         *
         * @return the status
         */
        public QualityGateStatus getStatus() {
            return status;
        }
    }

    /**
     * Available quality gate types.
     */
    public enum QualityGateType {
        TOTAL(StatisticProperties.TOTAL),
        TOTAL_ERROR(StatisticProperties.TOTAL_ERROR),
        TOTAL_HIGH(StatisticProperties.TOTAL_HIGH),
        TOTAL_NORMAL(StatisticProperties.TOTAL_NORMAL),
        TOTAL_LOW(StatisticProperties.TOTAL_LOW),

        NEW(StatisticProperties.NEW),
        NEW_ERROR(StatisticProperties.NEW_ERROR),
        NEW_HIGH(StatisticProperties.NEW_HIGH),
        NEW_NORMAL(StatisticProperties.NEW_NORMAL),
        NEW_LOW(StatisticProperties.NEW_LOW),

        DELTA(StatisticProperties.DELTA),
        DELTA_ERROR(StatisticProperties.DELTA_ERROR),
        DELTA_HIGH(StatisticProperties.DELTA_HIGH),
        DELTA_NORMAL(StatisticProperties.DELTA_NORMAL),
        DELTA_LOW(StatisticProperties.DELTA_LOW);

        private final StatisticProperties properties;

        QualityGateType(final StatisticProperties statisticProperties) {
            properties = statisticProperties;
        }

        /**
         * Returns the localized human-readable name of this type.
         *
         * @return human-readable name
         */
        public String getDisplayName() {
            return properties.getDisplayName();
        }

        /**
         * Returns the method that should be used to determine the actual number of issues in the build.
         *
         * @return the threshold getter
         */
        public Function<IssuesStatistics, Integer> getSizeGetter() {
            return properties.getSizeGetter();
        }
    }

    /**
     * Descriptor of the {@link QualityGate}.
     */
    @Extension
    public static class QualityGateDescriptor extends Descriptor<QualityGate> {
        private final ModelValidation modelValidation = new ModelValidation();
        private final JenkinsFacade jenkins;

        @VisibleForTesting
        QualityGateDescriptor(final JenkinsFacade jenkinsFacade) {
            super();

            jenkins = jenkinsFacade;
        }

        /**
         * Creates a new descriptor.
         */
        public QualityGateDescriptor() {
            this(new JenkinsFacade());
        }

        /**
         * Return the model for the select widget.
         *
         * @return the quality gate types
         */
        @POST
        public ListBoxModel doFillTypeItems() {
            ListBoxModel model = new ListBoxModel();

            if (jenkins.hasPermission(Jenkins.READ)) {
                for (QualityGateType qualityGateType : QualityGateType.values()) {
                    model.add(qualityGateType.getDisplayName(), qualityGateType.name());
                }
            }

            return model;
        }

        /**
         * Performs on-the-fly validation of the quality gate threshold.
         *
         * @param project
         *         the project that is configured
         * @param threshold
         *         the threshold
         *
         * @return the validation result
         */
        @POST
        public FormValidation doCheckThreshold(@AncestorInPath final BuildableItem project,
                @QueryParameter final int threshold) {
            if (!jenkins.hasPermission(Item.CONFIGURE, project)) {
                return FormValidation.ok();
            }
            return modelValidation.validateThreshold(threshold);
        }
    }
}
