package io.jenkins.plugins.analysis.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.jvnet.localizer.Localizable;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import static io.jenkins.plugins.analysis.core.util.QualityGate.GateStrength.*;

/**
 * Defines a quality gate based on a specific size of issues (total, new, delta) in the current build. After a build has
 * been finished, a set of {@link QualityGate quality gates} will be evaluated and the overall quality gate status will
 * be reported in Jenkins UI.
 *
 * @author Ullrich Hafner
 */
public class QualityGate extends AbstractDescribableImpl<QualityGate> implements Serializable {
    private static final long serialVersionUID = -397278599489416668L;

    private final int size;
    private final QualityGateType type;
    private final QualityGateStatus status;

    /**
     * Creates a new instance of {@link QualityGate}.
     *
     * @param size
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param warning
     *         determines whether the quality gate is a warning or failure
     */
    @DataBoundConstructor
    public QualityGate(final int size, final QualityGateType type, final boolean warning) {
        this.size = size;
        this.type = type;
        status = warning ? QualityGateStatus.WARNING : QualityGateStatus.FAILED;
    }

    public QualityGateType getType() {
        return type;
    }

    public boolean getWarning() {
        return status == QualityGateStatus.WARNING;
    }

    /**
     * Creates a new instance of {@link QualityGate}.
     *
     * @param size
     *         the minimum number of issues that fails the quality gate
     * @param type
     *         the type of the quality gate
     * @param strength
     *         determines whether the quality gate is a warning or failure
     */
    public QualityGate(final int size, final QualityGateType type, final GateStrength strength) {
        this.size = size;
        this.type = type;
        status = strength.status;
    }

    /**
     * Returns the minimum number of issues that will fail the quality gate.
     *
     * @return minimum number of issues
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the method that should be used to determine the actual number of issues in the build.
     *
     * @return size getter
     */
    public Function<IssuesStatistics, Integer> getActualSizeMethodReference() {
        return type.getSizeGetter();
    }

    /**
     * Returns the human readable name of the quality gate.
     *
     * @return the human readable name
     */
    public String getName() {
        return type.getDisplayName();
    }

    /**
     * Returns the quality gate status if the gate has not been passed.
     *
     * @return the status
     */
    public QualityGateStatus getStatus() {
        return status;
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
        return size == that.size && type == that.type && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, type, status);
    }

    /**
     * Determines whether the the quality gate evaluation creates a warning or failure if the gate has not been passed.
     */
    public enum GateStrength {
        /** The build will be marked as unstable. */
        WARNING(QualityGateStatus.WARNING),

        /** The build will be marked as failed. */
        FAILURE(QualityGateStatus.FAILED);

        private QualityGateStatus status;

        GateStrength(final QualityGateStatus status) {
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
     * Maps the old style {@link Thresholds} to the new style list of {@link QualityGate} instances.
     *
     * @param thresholds
     *         the thresholds
     *
     * @return the list of quality gates
     */
    public static List<QualityGate> map(final Thresholds thresholds) {
        List<QualityGate> gates = new ArrayList<>();
        if (thresholds.failedTotalAll > 0) {
            gates.add(new QualityGate(thresholds.failedTotalAll, QualityGateType.TOTAL, FAILURE));
        }
        if (thresholds.failedTotalHigh > 0) {
            gates.add(new QualityGate(thresholds.failedTotalHigh, QualityGateType.TOTAL_HIGH, FAILURE));
        }
        if (thresholds.failedTotalNormal > 0) {
            gates.add(new QualityGate(thresholds.failedTotalNormal, QualityGateType.TOTAL_NORMAL, FAILURE));
        }
        if (thresholds.failedTotalLow > 0) {
            gates.add(new QualityGate(thresholds.failedTotalLow, QualityGateType.TOTAL_LOW, FAILURE));
        }

        if (thresholds.unstableTotalAll > 0) {
            gates.add(new QualityGate(thresholds.unstableTotalAll, QualityGateType.TOTAL, WARNING));
        }
        if (thresholds.unstableTotalHigh > 0) {
            gates.add(new QualityGate(thresholds.unstableTotalHigh, QualityGateType.TOTAL_HIGH, WARNING));
        }
        if (thresholds.unstableTotalNormal > 0) {
            gates.add(new QualityGate(thresholds.unstableTotalNormal, QualityGateType.TOTAL_NORMAL, WARNING));
        }
        if (thresholds.unstableTotalLow > 0) {
            gates.add(new QualityGate(thresholds.unstableTotalLow, QualityGateType.TOTAL_LOW, WARNING));
        }

        if (thresholds.failedNewAll > 0) {
            gates.add(new QualityGate(thresholds.failedNewAll, QualityGateType.NEW, FAILURE));
        }
        if (thresholds.failedNewHigh > 0) {
            gates.add(new QualityGate(thresholds.failedNewHigh, QualityGateType.NEW_HIGH, FAILURE));
        }
        if (thresholds.failedNewNormal > 0) {
            gates.add(new QualityGate(thresholds.failedNewNormal, QualityGateType.NEW_NORMAL, FAILURE));
        }
        if (thresholds.failedNewLow > 0) {
            gates.add(new QualityGate(thresholds.failedNewLow, QualityGateType.NEW_LOW, FAILURE));
        }

        if (thresholds.unstableNewAll > 0) {
            gates.add(new QualityGate(thresholds.unstableNewAll, QualityGateType.NEW, WARNING));
        }
        if (thresholds.unstableNewHigh > 0) {
            gates.add(new QualityGate(thresholds.unstableNewHigh, QualityGateType.NEW_HIGH, WARNING));
        }
        if (thresholds.unstableNewNormal > 0) {
            gates.add(new QualityGate(thresholds.unstableNewNormal, QualityGateType.NEW_NORMAL, WARNING));
        }
        if (thresholds.unstableNewLow > 0) {
            gates.add(new QualityGate(thresholds.unstableNewLow, QualityGateType.NEW_LOW, WARNING));
        }

        return gates;
    }

    /**
     * Available quality gate types.
     */
    public enum QualityGateType {
        /** Total number of issues. */
        TOTAL(Messages._QualityGate_Type_Total(), IssuesStatistics::getTotalSize),
        /** Total number of issues (severity Error). */
        TOTAL_ERROR(Messages._QualityGate_Type_Total_Error(), IssuesStatistics::getTotalErrorSize),
        /** Total number of issues (severity Warning High). */
        TOTAL_HIGH(Messages._QualityGate_Type_Total_High(), IssuesStatistics::getTotalHighSize),
        /** Total number of issues (severity Warning Normal). */
        TOTAL_NORMAL(Messages._QualityGate_Type_Total_Normal(), IssuesStatistics::getTotalNormalSize),
        /** Total number of issues (severity Warning Low). */
        TOTAL_LOW(Messages._QualityGate_Type_Total_Low(), IssuesStatistics::getTotalLowSize),

        /** Number of new issues. */
        NEW(Messages._QualityGate_Type_New(), IssuesStatistics::getNewSize),
        /** Number of new issues (severity Error). */
        NEW_ERROR(Messages._QualityGate_Type_New_Error(), IssuesStatistics::getNewErrorSize),
        /** Number of new issues (severity Warning High). */
        NEW_HIGH(Messages._QualityGate_Type_New_High(), IssuesStatistics::getNewHighSize),
        /** Number of new issues (severity Warning Normal). */
        NEW_NORMAL(Messages._QualityGate_Type_New_Normal(), IssuesStatistics::getNewNormalSize),
        /** Number of new issues (severity Warning Low). */
        NEW_LOW(Messages._QualityGate_Type_New_Low(), IssuesStatistics::getNewLowSize),

        /** Delta current build - reference build. */
        DELTA(Messages._QualityGate_Type_Delta(), IssuesStatistics::getDeltaSize),
        /** Delta current build - reference build (severity Error). */
        DELTA_ERROR(Messages._QualityGate_Type_Delta_Error(), IssuesStatistics::getDeltaErrorSize),
        /** Delta current build - reference build (severity Warning High). */
        DELTA_HIGH(Messages._QualityGate_Type_Delta_High(), IssuesStatistics::getDeltaHighSize),
        /** Delta current build - reference build (severity Warning Normal). */
        DELTA_NORMAL(Messages._QualityGate_Type_Delta_Normal(), IssuesStatistics::getDeltaNormalSize),
        /** Delta current build - reference build (severity Warning Low). */
        DELTA_LOW(Messages._QualityGate_Type_Delta_Low(), IssuesStatistics::getDeltaLowSize);

        private final Localizable displayName;
        private final Function<IssuesStatistics, Integer> sizeGetter;

        QualityGateType(final Localizable displayName, final Function<IssuesStatistics, Integer> sizeGetter) {
            this.displayName = displayName;
            this.sizeGetter = sizeGetter;
        }

        /**
         * Returns the localized human readable name of this type.
         *
         * @return human readable name
         */
        public String getDisplayName() {
            return displayName.toString();
        }

        /**
         * Returns the method that should be used to determine the actual number of issues in the build.
         *
         * @return the size getter
         */
        public Function<IssuesStatistics, Integer> getSizeGetter() {
            return sizeGetter;
        }
    }

    /**
     * Descriptor of the {@link QualityGate}.
     */
    @Extension
    public static class QualityGateDescriptor extends Descriptor<QualityGate> {
        /**
         * Return the model for the select widget.
         *
         * @return the quality gate types
         */
        public ListBoxModel doFillTypeItems() {
            ListBoxModel model = new ListBoxModel();

            for (QualityGateType qualityGateType : QualityGateType.values()) {
                model.add(qualityGateType.getDisplayName(), qualityGateType.name());
            }

            return model;
        }

        /**
         * Performs on-the-fly validation of the quality gate threshold.
         *
         * @param size
         *         the threshold
         *
         * @return the validation result
         */
        public FormValidation doCheckSize(@QueryParameter final int size) {
            if (size > 0) {
                return FormValidation.ok();
            }
            return FormValidation.error(Messages.FieldValidator_Error_NegativeThreshold());
        }
    }
}
