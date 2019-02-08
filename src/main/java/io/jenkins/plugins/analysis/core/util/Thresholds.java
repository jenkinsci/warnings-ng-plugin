package io.jenkins.plugins.analysis.core.util;

import java.io.Serializable;

/**
 * Data object that simply stores the thresholds.
 *
 * @author Ullrich Hafner
 * @deprecated replaced by {@link QualityGate}
 */
@SuppressWarnings("all")
@Deprecated
public class Thresholds implements Serializable {
    private static final long serialVersionUID = 7500037879938406050L;

    public int unstableTotalAll = 0;
    public int unstableTotalHigh = 0;
    public int unstableTotalNormal = 0;
    public int unstableTotalLow = 0;
    public int unstableNewAll = 0;
    public int unstableNewHigh = 0;
    public int unstableNewNormal = 0;
    public int unstableNewLow = 0;
    public int failedTotalAll = 0;
    public int failedTotalHigh = 0;
    public int failedTotalNormal = 0;
    public int failedTotalLow = 0;
    public int failedNewAll = 0;
    public int failedNewHigh = 0;
    public int failedNewNormal = 0;
    public int failedNewLow = 0;
}

