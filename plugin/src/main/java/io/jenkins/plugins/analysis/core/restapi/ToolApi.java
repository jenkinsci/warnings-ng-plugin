package io.jenkins.plugins.analysis.core.restapi;

import edu.hm.hafner.analysis.Severity;

import java.util.Map;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Remote API model for a static analysis tool that has been invoked in a build. Simple Java Bean that
 * exposes several methods of a tool instance.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class ToolApi {
    private final String name;
    private final String id;
    private final String latestUrl;
    private final int size;
    private final Map<Severity, Integer> sizePerSeverity;

    /**
     * Creates a new instance of {@link ToolApi}.
     *
     * @param id
     *         unique ID of the tool
     * @param name
     *         human-readable name of the tool
     * @param latestUrl
     *         the URL to the latest results
     * @param size
     *         the number of warnings
     * @param sizePerSeverity
     *         the number of warnings, grouped by severity
     */
    public ToolApi(final String id, final String name, final String latestUrl, final int size, final Map<Severity, Integer> sizePerSeverity) {
        this.name = name;
        this.id = id;
        this.latestUrl = latestUrl;
        this.size = size;
        this.sizePerSeverity = sizePerSeverity;
    }

    @Exported
    public String getId() {
        return id;
    }

    @Exported
    public String getName() {
        return name;
    }

    @Exported
    public int getSize() {
        return size;
    }

    @Exported
    public String getLatestUrl() {
        return latestUrl;
    }

    @Exported
    public int getErrorSize() {
        return sizePerSeverity.getOrDefault(Severity.ERROR, 0);
    }

    @Exported
    public int getHighSize() {
        return sizePerSeverity.getOrDefault(Severity.WARNING_HIGH, 0);
    }

    @Exported
    public int getNormalSize() {
        return sizePerSeverity.getOrDefault(Severity.WARNING_NORMAL, 0);
    }

    @Exported
    public int getLowSize() {
        return sizePerSeverity.getOrDefault(Severity.WARNING_LOW, 0);
    }
}
