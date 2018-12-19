package io.jenkins.plugins.analysis.core.restapi;

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

    /**
     * Creates a new instance of {@link ToolApi}.
     *
     * @param id
     *         unique ID of the tool
     * @param name
     *         human readable name of the tool
     * @param latestUrl
     *         the URL to the latest results
     * @param size
     *         the number of warnings
     */
    public ToolApi(final String id, final String name, final String latestUrl, final int size) {
        this.name = name;
        this.id = id;
        this.latestUrl = latestUrl;
        this.size = size;
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
}
