package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import hudson.model.Actionable;
import hudson.model.ModelObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import tools.jackson.databind.ObjectMapper;

/**
 * Server side model that provides the data for the "Hotspots" page of the Warnings tab of a run. The page answers the
 * question: "Where should I start to clean up?". It shows the files (or categories, types, packages, or modules) with
 * the most warnings as a horizontal bar chart, stacked by severity, and a histogram that shows how the warnings are
 * distributed over all files (or categories, types, packages, or modules).
 *
 * <p>
 * The following settings can be changed on the client side in the app bar, see {@code hotspots-model.js}:
 * </p>
 * <ul>
 *     <li>the tool: all tools (only if the run has more than one result) or a single tool</li>
 *     <li>the property to group the warnings by, see {@link Grouping}</li>
 *     <li>the baseline: all, new, or outstanding warnings, see {@link Baseline}</li>
 *     <li>the number of hotspots to show</li>
 * </ul>
 *
 * <p>
 * The data is computed from the issues of the {@link AnalysisResult} of the selected {@link ResultAction results}.
 * Java provides only the raw data (see {@link HotspotsChart}); the charts are created on the client side with ECharts.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class HotspotsModel implements ModelObject {
    static final String URL = "hotspots";

    static final int MIN_HOTSPOTS = 1;
    static final int MAX_HOTSPOTS = 100;
    static final int DEFAULT_HOTSPOTS = 10;

    /** Upper bounds of the buckets of the histogram, the last bucket has no upper bound. */
    private static final List<Integer> BUCKET_LIMITS = List.of(1, 5, 10, 20, 50);

    private static final String GROUPING_PROPERTY = "grouping";
    private static final String HOTSPOTS_PROPERTY = "numberOfHotspots";

    private final Actionable owner;
    private final List<ResultAction> actions;

    /**
     * Creates a new instance of {@link HotspotsModel}.
     *
     * @param owner
     *         the run that owns the results
     * @param actions
     *         the results of the run, in the order they should be shown
     */
    HotspotsModel(final Actionable owner, final List<ResultAction> actions) {
        this.owner = owner;
        this.actions = actions.stream()
                .filter(action -> action.getResult().getTotalSize() > 0)
                .toList();
    }

    @Override
    public String getDisplayName() {
        return Messages.HotspotsModel_DisplayName();
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
     * Returns the results of the run that contain issues (one for each static analysis tool or aggregated result).
     * Tools without issues have no hotspots, so they are not listed.
     *
     * @return the results with issues
     */
    public List<ResultAction> getTools() {
        return actions;
    }

    /**
     * Returns whether the run has more than one result with issues. Only in this case the tool select box offers all tools.
     *
     * @return {@code true} if there are several tools, {@code false} otherwise
     */
    public boolean hasSeveralTools() {
        return actions.size() > 1;
    }

    /**
     * Returns the ID of the selection that combines all tools.
     *
     * @return the ID for all tools
     */
    public String getAllToolsId() {
        return TrendModel.ALL_TOOLS_ID;
    }

    /**
     * Returns whether at least one of the results contains issues.
     *
     * @return {@code true} if there are issues, {@code false} otherwise
     */
    public boolean hasIssues() {
        return !actions.isEmpty();
    }

    /**
     * Returns the properties the issues can be grouped by.
     *
     * @return the groupings
     */
    public List<Grouping> getGroupings() {
        return List.of(Grouping.values());
    }

    /**
     * Returns the baselines the issues can be selected from.
     *
     * @return the baselines
     */
    public List<Baseline> getBaselines() {
        return List.of(Baseline.values());
    }

    /**
     * Returns the hotspots for the specified configuration. The configuration is a JSON object with the properties
     * {@code tool} (the ID of a tool or {@value TrendModel#ALL_TOOLS_ID} for all tools), {@code grouping} (the ID of
     * the {@link Grouping}), {@code baseline} (the ID of the {@link Baseline}), and {@code numberOfHotspots}. Unknown
     * or missing values are replaced by defaults: all tools, files, all issues, and {@value #DEFAULT_HOTSPOTS}
     * hotspots.
     *
     * @param configuration
     *         JSON object to configure the hotspots
     *
     * @return the hotspots (converted to a JSON string)
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by hotspots-model.js
    public String getHotspots(final String configuration) {
        return new ObjectMapper().writeValueAsString(createHotspots(configuration));
    }

    /**
     * Creates the hotspots for the specified configuration, see {@link #getHotspots(String)}.
     *
     * @param configuration
     *         JSON object to configure the hotspots
     *
     * @return the hotspots
     */
    HotspotsChart createHotspots(final String configuration) {
        var properties = new ChartConfiguration(configuration);

        var grouping = Grouping.fromId(properties.getString(GROUPING_PROPERTY)).orElse(Grouping.FILE);
        var baseline = properties.getBaseline();
        var report = new Report();
        properties.selectTools(actions).forEach(action -> report.addAll(baseline.getIssues(action.getResult())));

        return new HotspotsChart(report, grouping, getNumberOfHotspots(properties));
    }

    /**
     * Returns the number of hotspots to show. The number is limited to the range {@value #MIN_HOTSPOTS} to
     * {@value #MAX_HOTSPOTS}, since the value is provided by the client.
     *
     * @param properties
     *         the properties of the configuration
     *
     * @return the number of hotspots
     */
    private int getNumberOfHotspots(final ChartConfiguration properties) {
        int count = properties.getInteger(HOTSPOTS_PROPERTY, DEFAULT_HOTSPOTS);
        return Math.max(MIN_HOTSPOTS, Math.min(MAX_HOTSPOTS, count));
    }

    /**
     * The properties the issues can be grouped by.
     */
    public enum Grouping {
        /** The affected file. */
        FILE("file", Issue::getFileName, Issue::getBaseName),
        /** The category. */
        CATEGORY("category", Issue::getCategory, Issue::getCategory),
        /** The type. */
        TYPE("type", Issue::getType, Issue::getType),
        /** The package or namespace. */
        PACKAGE("package", Issue::getPackageName, Issue::getPackageName),
        /** The module. */
        MODULE("module", Issue::getModuleName, Issue::getModuleName);

        private final String id;
        private final Function<Issue, String> property;
        private final Function<Issue, String> label;

        Grouping(final String id, final Function<Issue, String> property, final Function<Issue, String> label) {
            this.id = id;
            this.property = property;
            this.label = label;
        }

        /**
         * Returns the ID of this grouping, used as value in the select box and in the configuration.
         *
         * @return the ID
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the localized name of this grouping.
         *
         * @return the display name
         */
        public String getDisplayName() {
            return switch (this) {
                case FILE -> Messages.HotspotsModel_Grouping_File();
                case CATEGORY -> Messages.HotspotsModel_Grouping_Category();
                case TYPE -> Messages.HotspotsModel_Grouping_Type();
                case PACKAGE -> Messages.HotspotsModel_Grouping_Package();
                case MODULE -> Messages.HotspotsModel_Grouping_Module();
            };
        }

        String getProperty(final Issue issue) {
            return property.apply(issue);
        }

        String getLabel(final Issue issue) {
            return label.apply(issue);
        }

        /**
         * Returns the grouping with the specified ID.
         *
         * @param id
         *         the ID of the grouping
         *
         * @return the grouping, or an empty optional if there is no grouping with this ID
         */
        static Optional<Grouping> fromId(final String id) {
            return Arrays.stream(values())
                    .filter(grouping -> grouping.getId().equals(id))
                    .findFirst();
        }
    }

    /**
     * The issues of a result that are considered for the hotspots.
     */
    public enum Baseline {
        /** All issues of the build. */
        ALL("all"),
        /** Only the new issues, compared to the reference build. */
        NEW("new"),
        /** Only the outstanding issues, i.e., the issues that are part of the reference build as well. */
        OUTSTANDING("outstanding");

        private final String id;

        Baseline(final String id) {
            this.id = id;
        }

        /**
         * Returns the ID of this baseline, used as value in the select box and in the configuration. The IDs are the
         * same as on the issues page.
         *
         * @return the ID
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the localized name of this baseline.
         *
         * @return the display name
         */
        public String getDisplayName() {
            return switch (this) {
                case ALL -> Messages.HotspotsModel_Baseline_All();
                case NEW -> Messages.HotspotsModel_Baseline_New();
                case OUTSTANDING -> Messages.HotspotsModel_Baseline_Outstanding();
            };
        }

        Report getIssues(final AnalysisResult result) {
            return switch (this) {
                case ALL -> result.getIssues();
                case NEW -> result.getNewIssues();
                case OUTSTANDING -> result.getOutstandingIssues();
            };
        }

        /**
         * Returns the baseline with the specified ID.
         *
         * @param id
         *         the ID of the baseline
         *
         * @return the baseline, or an empty optional if there is no baseline with this ID
         */
        static Optional<Baseline> fromId(final String id) {
            return Arrays.stream(values())
                    .filter(baseline -> baseline.getId().equals(id))
                    .findFirst();
        }
    }

    /**
     * The data of the charts of the hotspots page: the hotspots with the most issues and the histogram of the number
     * of issues per element (e.g., per file). Serialized as JSON for {@code hotspots-model.js}.
     */
    @SuppressWarnings("PMD.DataClass")
    public static final class HotspotsChart {
        private final Map<String, String> severities;
        private final Map<String, String> colors;
        private final List<Hotspot> hotspots;
        private final List<Bucket> buckets;
        private final int numberOfElements;

        private HotspotsChart(final Report report, final Grouping grouping, final int numberOfHotspots) {
            severities = SeverityLegend.getLabels();
            colors = SeverityLegend.getColors();

            Map<String, Hotspot> elements = new HashMap<>();
            for (Issue issue : report) {
                elements.computeIfAbsent(
                                grouping.getProperty(issue), name -> new Hotspot(name, grouping.getLabel(issue)))
                        .add(issue.getSeverity());
            }
            var sorted = elements.values().stream()
                    .sorted(Comparator.comparingInt(Hotspot::getTotal)
                            .reversed()
                            .thenComparing(Hotspot::getName))
                    .toList();

            hotspots = sorted.stream().limit(numberOfHotspots).toList();
            buckets = createBuckets(sorted);
            numberOfElements = sorted.size();
        }

        private static List<Bucket> createBuckets(final List<Hotspot> elements) {
            var buckets = new ArrayList<Bucket>();
            int lower = 1;
            for (int limit : BUCKET_LIMITS) {
                buckets.add(new Bucket(lower, limit, elements));
                lower = limit + 1;
            }
            buckets.add(new Bucket(lower, Integer.MAX_VALUE, elements));
            return List.copyOf(buckets);
        }

        /**
         * Returns the severities of the issues, mapped from the ID (that is used as key of the counts of a
         * {@link Hotspot} and as CSS modifier of the color) to the localized name. Ordered from the most to the least
         * important severity.
         *
         * @return the severities
         */
        public Map<String, String> getSeverities() {
            return severities;
        }

        /**
         * Returns the colors of the severities, mapped from the ID (see {@link #getSeverities()}) to the name of the
         * CSS variable of the Jenkins palette, e.g., {@code --red}. These are the same colors as in the other charts
         * of the Warnings plugin, see {@link io.jenkins.plugins.analysis.core.charts.SeverityPalette}.
         *
         * @return the colors of the severities
         */
        public Map<String, String> getColors() {
            return colors;
        }

        /**
         * Returns the elements with the most issues, ordered by the number of issues (descending).
         *
         * @return the hotspots
         */
        public List<Hotspot> getHotspots() {
            return hotspots;
        }

        /**
         * Returns the buckets of the histogram: the number of elements (e.g., files) with a given number of issues.
         *
         * @return the buckets
         */
        public List<Bucket> getBuckets() {
            return buckets;
        }

        /**
         * Returns the number of elements (e.g., files) that contain at least one issue.
         *
         * @return the number of elements with issues
         */
        public int getNumberOfElements() {
            return numberOfElements;
        }
    }

    /**
     * An element (e.g., a file) and the number of its issues by severity.
     */
    public static final class Hotspot {
        private final String name;
        private final String label;
        private final int[] counts = new int[SeverityLegend.size()];

        private Hotspot(final String name, final String label) {
            this.name = name;
            this.label = label;
        }

        private void add(final Severity severity) {
            counts[SeverityLegend.indexOf(severity)]++;
        }

        /**
         * Returns the name of the element, e.g., the absolute path of a file. Shown in the tooltip.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns a short label of the element, e.g., the base name of a file. Shown at the axis of the chart.
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Returns the number of issues by severity. The keys are the IDs of {@link HotspotsChart#getSeverities()}.
         * Issues with an unknown severity are counted as normal.
         *
         * @return the number of issues by severity
         */
        public Map<String, Integer> getCounts() {
            return SeverityLegend.toMap(counts);
        }

        /**
         * Returns the total number of issues of the element.
         *
         * @return the number of issues
         */
        public int getTotal() {
            return Arrays.stream(counts).sum();
        }
    }

    /**
     * A bucket of the histogram: the number of elements that have a number of issues within the bounds of the bucket.
     */
    public static final class Bucket {
        private final String label;
        private final int count;

        private Bucket(final int lower, final int upper, final List<Hotspot> elements) {
            if (upper == Integer.MAX_VALUE) {
                label = "> " + (lower - 1);
            } else if (lower == upper) {
                label = String.valueOf(lower);
            } else {
                label = lower + "–" + upper;
            }
            count = (int) elements.stream()
                    .mapToInt(Hotspot::getTotal)
                    .filter(total -> total >= lower && total <= upper)
                    .count();
        }

        /**
         * Returns the label of the bucket, e.g., {@code 2–5}.
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Returns the number of elements in this bucket. Note that this property must not be named {@code size},
         * since {@code size} is a reserved word in JEXL.
         *
         * @return the number of elements
         */
        public int getCount() {
            return count;
        }
    }
}
