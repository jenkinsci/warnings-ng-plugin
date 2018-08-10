package io.jenkins.plugins.analysis.core.quality;

import java.io.Serializable;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Data object that simply stores the thresholds.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("all")
@ExportedBean
public class Thresholds implements Serializable {
    private static final long serialVersionUID = 7500037879938406050L;

    @Exported
    public int unstableTotalAll = 0;
    @Exported
    public int unstableTotalHigh = 0;
    @Exported
    public int unstableTotalNormal = 0;
    @Exported
    public int unstableTotalLow = 0;
    @Exported
    public int unstableNewAll = 0;
    @Exported
    public int unstableNewHigh = 0;
    @Exported
    public int unstableNewNormal = 0;
    @Exported
    public int unstableNewLow = 0;
    @Exported
    public int failedTotalAll = 0;
    @Exported
    public int failedTotalHigh = 0;
    @Exported
    public int failedTotalNormal = 0;
    @Exported
    public int failedTotalLow = 0;
    @Exported
    public int failedNewAll = 0;
    @Exported
    public int failedNewHigh = 0;
    @Exported
    public int failedNewNormal = 0;
    @Exported
    public int failedNewLow = 0;
}

