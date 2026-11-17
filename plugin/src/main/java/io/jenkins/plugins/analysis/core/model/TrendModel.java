package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.ChartModelConfiguration.AxisType;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.util.VisibleForTesting;
import hudson.model.Actionable;
import hudson.model.ModelObject;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.charts.HealthTrendChart;
import io.jenkins.plugins.analysis.core.charts.NewVersusFixedTrendChart;
import io.jenkins.plugins.analysis.core.charts.SeverityTrendChart;
import io.jenkins.plugins.analysis.core.charts.ToolsTrendChart;
import io.jenkins.plugins.analysis.core.charts.TrendChart;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Server side model that provides the data for the "Trend" page of the Warnings tab of a run. The page answers the
 * question: "How did the number of warnings evolve in the builds up to this build?". It shows a trend chart for one
 * of the {@link ResultAction results} of the run or for all results together. The history ends with the run that
 * owns this page, so older builds show the trend up to that build (and not up to the latest build of the job).
 *
 * <p>
 * The following settings can be changed on the client side in the app bar, see {@code trend-model.js}:
 * </p>
 * <ul>
 *     <li>the trend source: all tools (only if the run has more than one result) or a single tool</li>
 *     <li>the chart type: issues by severity, issues and health, new and fixed issues, or issues per tool. Not all
 *     chart types are available for each source, see {@link TrendSource#getChartTypes()}.</li>
 *     <li>the number of builds to show</li>
 * </ul>
 *
 * <p>
 * The chart models are created by the existing trend charts of the package
 * {@link io.jenkins.plugins.analysis.core.charts}, which are also used on the job page. The history of a single tool
 * is provided by {@link ResultAction#createBuildHistory()}, the history of all tools by
 * {@link CompositeBuildResultsIterable}, which sums up the results of all tools of a build.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class TrendModel implements ModelObject {
    static final String URL = "trend";
    /** The ID of the trend source that combines all tools. Not a valid ID of a tool, so there are no collisions. */
    static final String ALL_TOOLS_ID = "*";

    static final int MIN_BUILDS = 2;
    static final int MAX_BUILDS = 500;

    private static final String TOOL_PROPERTY = "tool";
    private static final String CHART_PROPERTY = "chart";
    private static final String NUMBER_OF_BUILDS_PROPERTY = "numberOfBuilds";
    private static final int DEFAULT_BUILDS = 50;
    private static final HealthDescriptor NO_HEALTH_REPORT = new HealthDescriptor(0, 0, Severity.WARNING_NORMAL);

    private final Actionable owner;
    private final List<TrendSource> sources;

    /**
     * Creates a new instance of {@link TrendModel}.
     *
     * @param owner
     *         the run that owns the results
     * @param actions
     *         the results of the run, in the order they should be shown
     */
    TrendModel(final Actionable owner, final List<ResultAction> actions) {
        this(owner, actions, new HistoryFacade());
    }

    /**
     * Creates a new instance of {@link TrendModel} with a custom facade to create the history of all tools.
     *
     * @param owner
     *         the run that owns the results
     * @param actions
     *         the results of the run, in the order they should be shown
     * @param facade
     *         the facade that creates the history of all tools and resolves the names of the tools
     */
    @VisibleForTesting
    TrendModel(final Actionable owner, final List<ResultAction> actions, final HistoryFacade facade) {
        this.owner = owner;
        this.sources = createSources(owner, actions, facade);
    }

    private static List<TrendSource> createSources(
            final Actionable owner, final List<ResultAction> actions, final HistoryFacade facade) {
        List<TrendSource> trendSources = new ArrayList<>();
        if (actions.size() != 1) {
            Set<String> origins = actions.stream()
                    .map(action -> action.getResult().getSizePerOrigin().keySet())
                    .flatMap(Set::stream)
                    .collect(Collectors.toCollection(TreeSet::new));
            trendSources.add(new TrendSource(
                    ALL_TOOLS_ID,
                    Messages.TrendModel_AllTools(),
                    () -> facade.createCompositeHistory(owner),
                    NO_HEALTH_REPORT,
                    origins,
                    () -> facade.createToolNames(owner, origins)));
        }
        for (ResultAction action : actions) {
            Set<String> origins =
                    Set.copyOf(action.getResult().getSizePerOrigin().keySet());
            trendSources.add(new TrendSource(
                    action.getId(),
                    action.getDisplayName(),
                    action::createBuildHistory,
                    action.getHealthDescriptor(),
                    origins,
                    () -> facade.createToolNames(owner, origins)));
        }
        return List.copyOf(trendSources);
    }

    @Override
    public String getDisplayName() {
        return Messages.TrendModel_DisplayName();
    }

    /**
     * Returns the URL of this page, relative to the Warnings tab.
     *
     * @return the URL name
     */
    public String getUrlName() {
        return URL;
    }

    /**
     * Returns the run that owns the results. Required by {@code l:run-subpage}.
     *
     * @return the run
     */
    public Actionable getObject() {
        return owner;
    }

    /**
     * Returns the Warnings tab of the run. Used to render the app bar and the side panel.
     *
     * @return the tab
     */
    public RunTab getTab() {
        return new RunTab(owner);
    }

    /**
     * Returns the sources that can be selected for the trend chart. If the run has more than one result, the first
     * source combines all tools. Then a source for each tool follows.
     *
     * @return the trend sources, never empty
     */
    public List<TrendSource> getSources() {
        return sources;
    }

    /**
     * Returns all chart types. Which of them are available for a given source is determined on the client side using
     * {@link TrendSource#getChartIds()}.
     *
     * @return all chart types
     */
    public List<ChartType> getChartTypes() {
        return List.of(ChartType.values());
    }

    /**
     * Returns the trend chart for the specified configuration. The configuration is a JSON object with the
     * properties {@code tool} (the ID of the {@link TrendSource}), {@code chart} (the ID of the {@link ChartType}),
     * and {@code numberOfBuilds}. Unknown or missing values are replaced by defaults: the first source, the
     * {@link TrendSource#getDefaultChartType() default chart type} of the source, and 50 builds.
     *
     * @param configuration
     *         JSON object to configure the trend chart
     *
     * @return the trend chart model (converted to a JSON string)
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by trend-model.js
    public String getTrendChart(final String configuration) {
        return new ObjectMapper().writeValueAsString(createTrendChart(configuration));
    }

    /**
     * Creates the trend chart for the specified configuration, see {@link #getTrendChart(String)}.
     *
     * @param configuration
     *         JSON object to configure the trend chart
     *
     * @return the trend chart model
     */
    LinesChartModel createTrendChart(final String configuration) {
        var properties = parse(configuration);

        var source = findSource(getString(properties, TOOL_PROPERTY));
        var chartType = ChartType.fromId(getString(properties, CHART_PROPERTY))
                .filter(source::isAvailable)
                .orElse(source.getDefaultChartType());

        return source.createChart(chartType, createChartConfiguration(properties));
    }

    private TrendSource findSource(final String id) {
        return sources.stream()
                .filter(source -> source.getId().equals(id))
                .findFirst()
                .orElse(sources.get(0));
    }

    private ObjectNode parse(final String configuration) {
        var mapper = new ObjectMapper();
        try {
            if (mapper.readTree(configuration) instanceof ObjectNode properties) {
                return properties;
            }
            return mapper.createObjectNode();
        } catch (JacksonException exception) {
            return mapper.createObjectNode();
        }
    }

    private String getString(final ObjectNode properties, final String property) {
        return properties.optional(property).map(value -> value.asString("")).orElse("");
    }

    /**
     * Creates the chart configuration. The number of builds is limited to the range {@value #MIN_BUILDS} to
     * {@value #MAX_BUILDS}, since the value is provided by the client and every build of the history needs to be
     * loaded.
     *
     * @param properties
     *         the properties of the configuration
     *
     * @return the chart configuration
     */
    private ChartModelConfiguration createChartConfiguration(final ObjectNode properties) {
        int buildCount = properties
                .optional(NUMBER_OF_BUILDS_PROPERTY)
                .map(value -> value.asInt(DEFAULT_BUILDS))
                .orElse(DEFAULT_BUILDS);
        return new ChartModelConfiguration(AxisType.BUILD, Math.max(MIN_BUILDS, Math.min(MAX_BUILDS, buildCount)), 0);
    }

    /**
     * The types of trend charts that can be shown on the trend page.
     */
    public enum ChartType {
        /** Stacked number of issues by severity. */
        SEVERITY("severity"),
        /** Number of issues, colored by the health report thresholds. Requires an enabled health report. */
        HEALTH("health"),
        /** Number of new and fixed issues. */
        NEW_VERSUS_FIXED("new"),
        /** Number of issues per tool. Requires at least two tools. */
        TOOLS("tools");

        private final String id;

        ChartType(final String id) {
            this.id = id;
        }

        /**
         * Returns the ID of this chart type, used as value in the select box and in the configuration.
         *
         * @return the ID
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the localized name of this chart type.
         *
         * @return the display name
         */
        public String getDisplayName() {
            return switch (this) {
                case SEVERITY -> Messages.TrendModel_Chart_Severity();
                case HEALTH -> Messages.TrendModel_Chart_Health();
                case NEW_VERSUS_FIXED -> Messages.TrendModel_Chart_NewVersusFixed();
                case TOOLS -> Messages.TrendModel_Chart_Tools();
            };
        }

        /**
         * Returns the chart type with the specified ID.
         *
         * @param id
         *         the ID of the chart type
         *
         * @return the chart type, or an empty optional if there is no chart type with this ID
         */
        static Optional<ChartType> fromId(final String id) {
            return Arrays.stream(values())
                    .filter(type -> type.getId().equals(id))
                    .findFirst();
        }
    }

    /**
     * A source of a trend chart: either a single tool or the combination of all tools of a run.
     */
    public static final class TrendSource {
        private final String id;
        private final String displayName;
        private final Supplier<Iterable<BuildResult<AnalysisBuildResult>>> history;
        private final HealthDescriptor healthDescriptor;
        private final Set<String> origins;
        private final Supplier<Map<String, String>> toolNames;

        private TrendSource(
                final String id,
                final String displayName,
                final Supplier<Iterable<BuildResult<AnalysisBuildResult>>> history,
                final HealthDescriptor healthDescriptor,
                final Set<String> origins,
                final Supplier<Map<String, String>> toolNames) {
            this.id = id;
            this.displayName = displayName;
            this.history = history;
            this.healthDescriptor = healthDescriptor;
            this.origins = origins;
            this.toolNames = toolNames;
        }

        /**
         * Returns the ID of this source: the ID of the tool, or {@value TrendModel#ALL_TOOLS_ID} for all tools.
         *
         * @return the ID
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the name of this source.
         *
         * @return the display name
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Returns the chart types that are available for this source. The health chart requires an enabled health
         * report, so it is never available for all tools (each tool has its own thresholds). The chart with the issues
         * per tool requires at least two tools (or origins of an aggregated result).
         *
         * @return the available chart types
         */
        public List<ChartType> getChartTypes() {
            return Arrays.stream(ChartType.values()).filter(this::isAvailable).toList();
        }

        /**
         * Returns the IDs of the available chart types as a space separated list. Used as data attribute of the
         * options in the select box.
         *
         * @return the IDs of the available chart types
         */
        public String getChartIds() {
            return getChartTypes().stream().map(ChartType::getId).collect(Collectors.joining(" "));
        }

        /**
         * Returns the chart type that is shown if no (or an unavailable) chart type has been selected.
         *
         * @return the default chart type
         */
        public ChartType getDefaultChartType() {
            return isAvailable(ChartType.TOOLS) ? ChartType.TOOLS : ChartType.SEVERITY;
        }

        /**
         * Returns whether the specified chart type is available for this source.
         *
         * @param chartType
         *         the chart type to check
         *
         * @return {@code true} if the chart type is available, {@code false} otherwise
         */
        boolean isAvailable(final ChartType chartType) {
            return switch (chartType) {
                case SEVERITY, NEW_VERSUS_FIXED -> true;
                case HEALTH -> healthDescriptor.isEnabled();
                case TOOLS -> origins.size() > 1;
            };
        }

        /**
         * Creates the trend chart of the specified type for this source.
         *
         * @param chartType
         *         the type of the chart
         * @param configuration
         *         the chart configuration
         *
         * @return the chart model
         */
        LinesChartModel createChart(final ChartType chartType, final ChartModelConfiguration configuration) {
            TrendChart chart = switch (chartType) {
                case SEVERITY -> new SeverityTrendChart();
                case HEALTH -> new HealthTrendChart(healthDescriptor);
                case NEW_VERSUS_FIXED -> new NewVersusFixedTrendChart();
                case TOOLS -> new ToolsTrendChart(toolNames.get());
            };
            return chart.create(history.get(), configuration);
        }
    }

    /**
     * Facade to create the history of all tools and to resolve the names of the tools. Encapsulates the access to
     * the builds of Jenkins so that the model can be tested without a running Jenkins instance.
     */
    @VisibleForTesting
    static class HistoryFacade {
        /**
         * Creates the history of all tools, starting with the specified run.
         *
         * @param owner
         *         the run that owns the results
         *
         * @return the history of all tools, or an empty history if the owner is no run
         */
        Iterable<BuildResult<AnalysisBuildResult>> createCompositeHistory(final Actionable owner) {
            if (owner instanceof Run<?, ?> run) {
                return new CompositeBuildResultsIterable(run);
            }
            return List.of();
        }

        /**
         * Creates a mapping from the IDs of the specified tools (origins) to their human-readable names.
         *
         * @param owner
         *         the run that owns the results
         * @param origins
         *         the IDs of the tools
         *
         * @return the mapping of tool IDs to names
         */
        Map<String, String> createToolNames(final Actionable owner, final Set<String> origins) {
            if (owner instanceof Run<?, ?> run) {
                var registry = ToolNameRegistry.fromBuild(run);
                return origins.stream().collect(Collectors.toMap(Function.identity(), registry::getName));
            }
            return Map.of();
        }
    }
}
