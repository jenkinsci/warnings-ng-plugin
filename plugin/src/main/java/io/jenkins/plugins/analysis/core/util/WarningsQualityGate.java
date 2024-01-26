package io.jenkins.plugins.analysis.core.util;

import java.util.function.Function;

import edu.hm.hafner.util.VisibleForTesting;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import hudson.Extension;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;
import io.jenkins.plugins.util.JenkinsFacade;
import io.jenkins.plugins.util.QualityGate;

/**
 * Defines a quality gate based on a specific threshold of issues (total, new, delta) in the current build. After a
 * build has been finished, a set of {@link WarningsQualityGate quality gates} will be evaluated and the overall quality
 * gate status will be reported in Jenkins UI.
 *
 * @author Ullrich Hafner
 */
public class WarningsQualityGate extends QualityGate {
    private static final long serialVersionUID = -3560049414586166711L;

    private final QualityGateType type;

    /**
     * Creates a new instance of {@link WarningsQualityGate}.
     *
     * @param type
     *         the type of the quality gate
     */
    @DataBoundConstructor
    public WarningsQualityGate(final QualityGateType type) {
        super();

        this.type = type;
    }

    /**
     * Creates a new instance of {@link WarningsQualityGate}.
     *
     * @param threshold
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param criticality
     *         the criticality of the quality gate
     */
    public WarningsQualityGate(final int threshold, final QualityGateType type,
            final QualityGateCriticality criticality) {
        this(type);

        setIntegerThreshold(threshold);
        setCriticality(criticality);
    }

    public boolean isUnstable() {
        return getCriticality() == QualityGateCriticality.UNSTABLE
                || getCriticality() == QualityGateCriticality.NOTE;
    }

    /**
     * Sets the criticality of the quality gate.
     *
     * @param unstable
     *         the criticality of the quality gate
     * @deprecated use {@link #setCriticality(QualityGateCriticality)} instead
     */
    @DataBoundSetter
    @Deprecated
    public void setUnstable(final boolean unstable) {
        if (unstable) {
            setCriticality(QualityGateCriticality.UNSTABLE);
        }
        else {
            setCriticality(QualityGateCriticality.FAILURE);
        }
    }

    /**
     * Returns the method that should be used to determine the actual number of issues in the build.
     *
     * @return threshold getter
     */
    public Function<IssuesStatistics, Integer> getActualSizeMethodReference() {
        return type.getSizeGetter();
    }

    @Override
    public String getName() {
        return type.getDisplayName();
    }

    public QualityGateType getType() {
        return type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WarningsQualityGate that = (WarningsQualityGate) o;

        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
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
        TOTAL_MODIFIED(StatisticProperties.TOTAL_MODIFIED),

        NEW(StatisticProperties.NEW),
        NEW_ERROR(StatisticProperties.NEW_ERROR),
        NEW_HIGH(StatisticProperties.NEW_HIGH),
        NEW_NORMAL(StatisticProperties.NEW_NORMAL),
        NEW_LOW(StatisticProperties.NEW_LOW),
        NEW_MODIFIED(StatisticProperties.NEW_MODIFIED),

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
     * Descriptor of the {@link WarningsQualityGate}.
     */
    @Extension
    public static class WarningsQualityGateDescriptor extends QualityGateDescriptor {
        private final ModelValidation modelValidation = new ModelValidation();
        private final JenkinsFacade jenkins;

        @VisibleForTesting
        WarningsQualityGateDescriptor(final JenkinsFacade jenkinsFacade) {
            super();

            jenkins = jenkinsFacade;
        }

        /**
         * Creates a new descriptor.
         */
        public WarningsQualityGateDescriptor() {
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
