package hudson.plugins.analysis.core;

import org.apache.commons.lang.StringUtils;

/**
 * Data object that simply stores the thresholds.
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:OFF
@edu.umd.cs.findbugs.annotations.SuppressWarnings("")
@SuppressWarnings("all")
public class Thresholds {
    String unstableTotalAll = StringUtils.EMPTY;
    String unstableTotalHigh = StringUtils.EMPTY;
    String unstableTotalNormal = StringUtils.EMPTY;
    String unstableTotalLow = StringUtils.EMPTY;
    String unstableNewAll = StringUtils.EMPTY;
    String unstableNewHigh = StringUtils.EMPTY;
    String unstableNewNormal = StringUtils.EMPTY;
    String unstableNewLow = StringUtils.EMPTY;
    String failedTotalAll = StringUtils.EMPTY;
    String failedTotalHigh = StringUtils.EMPTY;
    String failedTotalNormal = StringUtils.EMPTY;
    String failedTotalLow = StringUtils.EMPTY;
    String failedNewAll = StringUtils.EMPTY;
    String failedNewHigh = StringUtils.EMPTY;
    String failedNewNormal = StringUtils.EMPTY;
    String failedNewLow = StringUtils.EMPTY;

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
     *            string representation of the threshold value
     * @return <code>true</code> if the provided threshold string parameter is a
     *         valid number >= 0
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
}

