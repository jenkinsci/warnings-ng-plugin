package hudson.plugins.analysis.core;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import static hudson.plugins.analysis.util.ThresholdValidator.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Data object that simply stores the thresholds.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:OFF
@SuppressFBWarnings("")
@SuppressWarnings("all")
@ExportedBean
public class Thresholds implements Serializable {
    @Exported
    public String unstableTotalAll = StringUtils.EMPTY;
    @Exported
    public String unstableTotalHigh = StringUtils.EMPTY;
    @Exported
    public String unstableTotalNormal = StringUtils.EMPTY;
    @Exported
    public String unstableTotalLow = StringUtils.EMPTY;
    @Exported
    public String unstableNewAll = StringUtils.EMPTY;
    @Exported
    public String unstableNewHigh = StringUtils.EMPTY;
    @Exported
    public String unstableNewNormal = StringUtils.EMPTY;
    @Exported
    public String unstableNewLow = StringUtils.EMPTY;
    @Exported
    public String failedTotalAll = StringUtils.EMPTY;
    @Exported
    public String failedTotalHigh = StringUtils.EMPTY;
    @Exported
    public String failedTotalNormal = StringUtils.EMPTY;
    @Exported
    public String failedTotalLow = StringUtils.EMPTY;
    @Exported
    public String failedNewAll = StringUtils.EMPTY;
    @Exported
    public String failedNewHigh = StringUtils.EMPTY;
    @Exported
    public String failedNewNormal = StringUtils.EMPTY;
    @Exported
    public String failedNewLow = StringUtils.EMPTY;

    /**
     * Returns whether at least one of the thresholds is set.
     *
     * @return <code>true</code> if at least one of the thresholds is set,
     *         <code>false</code> if no threshold is set
     */
    public boolean isValid() {
        return isValid(unstableTotalAll)
        || isValid(unstableTotalHigh)
        || isValid(unstableTotalNormal)
        || isValid(unstableTotalLow)
        || isValid(unstableNewAll)
        || isValid(unstableNewHigh)
        || isValid(unstableNewNormal)
        || isValid(unstableNewLow)
        || isValid(failedTotalAll)
        || isValid(failedTotalHigh)
        || isValid(failedTotalNormal)
        || isValid(failedTotalLow)
        || isValid(failedNewAll)
        || isValid(failedNewHigh)
        || isValid(failedNewNormal)
        || isValid(failedNewLow);
    }

    /**
     * Returns whether the provided threshold string parameter is a valid
     * threshold number, i.e. an integer value greater or equal zero.
     *
     * @param threshold
     *        string representation of the threshold value
     * @return <code>true</code> if the provided threshold string parameter is a
     *         valid number greater or equal 0
     */
    public static boolean isValid(final String threshold) {
        if (StringUtils.isNotBlank(threshold)) {
            try {
                return Integer.valueOf(threshold) >= 0;
            }
            catch (NumberFormatException exception) {
                // not valid
            }
        }
        return false;
    }

    /**
     * Returns a lower bound of warnings that will guarantee that a build
     * neither is unstable or failed.
     *
     * @return the number of warnings
     */
    public int getLowerBound() {
        if (isValid(unstableTotalAll)) {
            return convert(unstableTotalAll);
        }
        if (isValid(failedTotalAll)) {
            return convert(failedTotalAll);
        }
        return 0;
    }
}

