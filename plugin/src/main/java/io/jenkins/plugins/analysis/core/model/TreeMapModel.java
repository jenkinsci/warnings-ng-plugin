package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import hudson.model.Actionable;
import hudson.model.ModelObject;
import io.jenkins.plugins.analysis.core.model.HotspotsModel.Baseline;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import tools.jackson.databind.ObjectMapper;

/**
 * Server side model that provides the data for the "Treemap" page of the Warnings tab of a run. The page answers the
 * question: "Which parts of the project contain the warnings?". It shows the hierarchy of the affected modules,
 * packages (or folders, if an issue has no package), and files as a treemap. Since the Warnings plugin does not know
 * the size of the files, the area of each element is the number of its warnings.
 *
 * <p>
 * The following settings can be changed on the client side in the app bar, see {@code treemap-model.js}:
 * </p>
 * <ul>
 *     <li>the tool: all tools (only if the run has more than one result) or a single tool</li>
 *     <li>the baseline: all, new, or outstanding warnings, see {@link Baseline}</li>
 *     <li>the coloring: the highest severity of the warnings of an element, or the share of new warnings</li>
 * </ul>
 *
 * <p>
 * The module level is only shown if the issues are spread over several modules. The data is computed from the issues
 * of the {@link AnalysisResult} of the selected {@link ResultAction results}. Java provides only the raw data (see
 * {@link TreeMapChart}); the chart and its colors are created on the client side with ECharts. The configuration is
 * parsed by {@link ChartConfiguration}, as on the {@link HotspotsModel hotspots page}.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class TreeMapModel implements ModelObject {
    static final String URL = "treemap";

    private final Actionable owner;
    private final List<ResultAction> actions;

    /**
     * Creates a new instance of {@link TreeMapModel}.
     *
     * @param owner
     *         the run that owns the results
     * @param actions
     *         the results of the run, in the order they should be shown
     */
    TreeMapModel(final Actionable owner, final List<ResultAction> actions) {
        this.owner = owner;
        this.actions = actions.stream()
                .filter(action -> action.getResult().getTotalSize() > 0)
                .toList();
    }

    @Override
    public String getDisplayName() {
        return Messages.TreeMapModel_DisplayName();
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
     * Tools without issues have no elements in the treemap, so they are not listed.
     *
     * @return the results with issues
     */
    public List<ResultAction> getTools() {
        return actions;
    }

    /**
     * Returns whether the run has more than one result with issues. Only in this case the tool select box offers all
     * tools.
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
     * Returns the baselines the issues can be selected from.
     *
     * @return the baselines
     */
    public List<Baseline> getBaselines() {
        return List.of(Baseline.values());
    }

    /**
     * Returns the treemap for the specified configuration. The configuration is a JSON object with the properties
     * {@code tool} (the ID of a tool or {@value TrendModel#ALL_TOOLS_ID} for all tools) and {@code baseline} (the ID
     * of the {@link Baseline}). Unknown or missing values are replaced by defaults: all tools and all issues.
     *
     * @param configuration
     *         JSON object to configure the treemap
     *
     * @return the treemap (converted to a JSON string)
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by treemap-model.js
    public String getTreeMap(final String configuration) {
        return new ObjectMapper().writeValueAsString(createTreeMap(configuration));
    }

    /**
     * Creates the treemap for the specified configuration, see {@link #getTreeMap(String)}.
     *
     * @param configuration
     *         JSON object to configure the treemap
     *
     * @return the treemap
     */
    TreeMapChart createTreeMap(final String configuration) {
        var properties = new ChartConfiguration(configuration);

        var tools = properties.selectTools(actions);
        var baseline = properties.getBaseline();
        var report = new Report();
        tools.forEach(action -> report.addAll(baseline.getIssues(action.getResult())));
        Set<UUID> newIssues = tools.stream()
                .map(action -> action.getResult().getNewIssues())
                .flatMap(Report::stream)
                .map(Issue::getId)
                .collect(Collectors.toSet());

        return new TreeMapChart(report, newIssues);
    }

    /**
     * The data of the treemap: the root of the hierarchy of modules, packages (or folders), and files. Serialized as
     * JSON for {@code treemap-model.js}.
     */
    public static final class TreeMapChart {
        private final Map<String, String> severities;
        private final Map<String, String> colors;
        private final Node root;

        private TreeMapChart(final Report report, final Set<UUID> newIssues) {
            severities = SeverityLegend.getLabels();
            colors = SeverityLegend.getColors();
            root = new Node("", "");

            boolean hasSeveralModules =
                    report.stream().map(Issue::getModuleName).distinct().count() > 1;
            for (Issue issue : report) {
                boolean isNew = newIssues.contains(issue.getId());
                var node = root.add(issue, isNew);
                if (hasSeveralModules) {
                    node = node.getChild(issue.getModuleName(), issue.getModuleName())
                            .add(issue, isNew);
                }
                var container = getContainer(issue);
                node = node.getChild(container, container).add(issue, isNew);
                node.getChild(issue.getFileName(), issue.getBaseName()).add(issue, isNew);
            }
        }

        private static String getContainer(final Issue issue) {
            if (issue.hasPackageName()) {
                return issue.getPackageName();
            }
            return issue.getFolder();
        }

        /**
         * Returns the severities of the issues, mapped from the ID (that is used as key of the counts of a
         * {@link Node}) to the localized name. Ordered from the most to the least important severity.
         *
         * @return the severities
         */
        public Map<String, String> getSeverities() {
            return severities;
        }

        /**
         * Returns the colors of the severities, mapped from the ID (see {@link #getSeverities()}) to the name of the
         * CSS variable of the Jenkins palette, e.g., {@code --red}.
         *
         * @return the colors of the severities
         */
        public Map<String, String> getColors() {
            return colors;
        }

        /**
         * Returns the root of the hierarchy. The root has no name, its children are the modules (if there are
         * several modules) or the packages (or folders).
         *
         * @return the root node
         */
        public Node getRoot() {
            return root;
        }
    }

    /**
     * An element of the hierarchy (module, package, folder, or file) and the number of its issues.
     */
    @SuppressWarnings("PMD.DataClass")
    public static final class Node {
        private final String name;
        private final String label;
        private final int[] counts = new int[SeverityLegend.size()];
        private int numberOfNewIssues;
        private final Map<String, Node> children = new TreeMap<>();

        private Node(final String name, final String label) {
            this.name = name;
            this.label = label;
        }

        private Node getChild(final String childName, final String childLabel) {
            return children.computeIfAbsent(childName, key -> new Node(childName, childLabel));
        }

        private Node add(final Issue issue, final boolean isNew) {
            counts[SeverityLegend.indexOf(issue.getSeverity())]++;
            if (isNew) {
                numberOfNewIssues++;
            }
            return this;
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
         * Returns a short label of the element, e.g., the base name of a file. Shown in the treemap.
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Returns the number of issues by severity. The keys are the IDs of {@link TreeMapChart#getSeverities()}.
         * Issues with an unknown severity are counted as normal.
         *
         * @return the number of issues by severity
         */
        public Map<String, Integer> getCounts() {
            return SeverityLegend.toMap(counts);
        }

        /**
         * Returns the total number of issues of the element. This value defines the area of the element.
         *
         * @return the number of issues
         */
        public int getTotal() {
            int total = 0;
            for (int count : counts) {
                total += count;
            }
            return total;
        }

        /**
         * Returns the number of new issues of the element, i.e., the issues that are not part of the reference build.
         *
         * @return the number of new issues
         */
        public int getNumberOfNewIssues() {
            return numberOfNewIssues;
        }

        /**
         * Returns the children of this element, sorted by name. Files have no children.
         *
         * @return the children
         */
        public List<Node> getChildren() {
            return List.copyOf(children.values());
        }
    }
}
