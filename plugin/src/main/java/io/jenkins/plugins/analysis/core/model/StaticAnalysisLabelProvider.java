package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Report.IssueType;
import edu.hm.hafner.util.Generated;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.jvnet.localizer.Localizable;
import hudson.model.Job;
import hudson.model.Run;

import static j2html.TagCreator.*;

/**
 * A generic label provider for static analysis results. Creates pre-defined labels that are parameterized with a string
 * placeholder, that will be replaced with the actual name of the static analysis tool. Moreover, such a default label
 * provider decorates the links and summary boxes with the default icon of the warnings plug-in.
 *
 * @author Ullrich Hafner
 */
public class StaticAnalysisLabelProvider implements DescriptionProvider {
    /** Default icon for all tools. */
    @VisibleForTesting
    public static final String ANALYSIS_SVG_ICON = "symbol-solid/triangle-exclamation plugin-font-awesome-api";

    /** Provides an empty description. */
    protected static final DescriptionProvider EMPTY_DESCRIPTION = Issue::getDescription;

    private final String id;
    private final String icon;
    @CheckForNull
    private String name;
    private final DescriptionProvider descriptionProvider;

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     */
    @VisibleForTesting
    StaticAnalysisLabelProvider(final String id) {
        this(id, StringUtils.EMPTY, EMPTY_DESCRIPTION);
    }

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     * @param name
     *         the name of the static analysis tool
     */
    public StaticAnalysisLabelProvider(final String id, @CheckForNull final String name) {
        this(id, name, EMPTY_DESCRIPTION);
    }

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     * @param name
     *         the name of the static analysis tool
     * @param descriptionProvider
     *         provides additional descriptions for an issue
     */
    public StaticAnalysisLabelProvider(final String id, @CheckForNull final String name,
            final DescriptionProvider descriptionProvider) {
        this(id, name, descriptionProvider, IssueType.WARNING);
    }

    /**
     * Creates a new {@link StaticAnalysisLabelProvider} with the specified ID.
     *
     * @param id
     *         the ID
     * @param name
     *         the name of the static analysis tool
     * @param descriptionProvider
     *         provides additional descriptions for an issue
     * @param type
     *        the type of the parser
     */
    public StaticAnalysisLabelProvider(final String id, @CheckForNull final String name,
            final DescriptionProvider descriptionProvider, final IssueType type) {
        this.id = id;
        this.descriptionProvider = descriptionProvider;
        this.icon = getIcon(type);

        changeName(name);
    }

    private String getIcon(final IssueType type) {
        switch (type) {
            case BUG:
                return "symbol-solid/bug plugin-font-awesome-api";
            case DUPLICATION:
                return "symbol-regular/clone plugin-font-awesome-api";
            case VULNERABILITY:
                return "symbol-solid/shield-halved plugin-font-awesome-api";
            default:
                return ANALYSIS_SVG_ICON;
        }
    }

    private void changeName(final String originalName) {
        if (StringUtils.isNotBlank(originalName) && !"-".equals(originalName)) { // don't overwrite with empty or "-"
            name = originalName;
        }
    }

    /**
     * Returns the model for the issues details table.
     *
     * @param build
     *         the build of the results
     * @param url
     *         the URL of the results
     * @param report
     *         the report to show
     *
     * @return the table model
     */
    public DetailsTableModel getIssuesModel(final Run<?, ?> build, final String url, final Report report) {
        return new IssuesModel(report, getFileNameRenderer(build), getAgeBuilder(build, url), this);
    }

    /**
     * Creates a {@link DefaultAgeBuilder} for the specified run and url.
     *
     * @param owner
     *         the run to get the age from
     * @param url
     *         the url to the results
     *
     * @return the age builder
     */
    protected DefaultAgeBuilder getAgeBuilder(final Run<?, ?> owner, final String url) {
        return new DefaultAgeBuilder(owner.getNumber(), url, owner.getParent());
    }

    /**
     * Creates a {@link FileNameRenderer} for the specified run.
     *
     * @param owner
     *         the run to get the file names for
     *
     * @return the age builder
     */
    protected FileNameRenderer getFileNameRenderer(final Run<?, ?> owner) {
        return new FileNameRenderer(owner);
    }

    @VisibleForTesting
    String getDefaultName() {
        return Messages.Tool_Default_Name();
    }

    /**
     * Returns the ID of the tool.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the human-readable name of the tool. If the name has not been set, then the default name is returned.
     *
     * @return the name
     */
    public String getName() {
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        return getDefaultName();
    }

    /**
     * Sets the human-readable name of the tool.
     *
     * @param name
     *         the name of the tool
     *
     * @return the name
     */
    public StaticAnalysisLabelProvider setName(@CheckForNull final String name) {
        changeName(name);

        return this;
    }

    @Override
    @Generated
    public String toString() {
        return String.format("%s: %s", getId(), getName());
    }

    /**
     * Returns the name of the link to the results.
     *
     * @return the name of the side panel link
     */
    public String getLinkName() {
        if (StringUtils.isNotBlank(name)) {
            return Messages.Tool_Link_Name(name);
        }
        return Messages.Tool_Link_Name(getDefaultName());
    }

    /**
     * Returns the legend for the trend chart in the project overview.
     *
     * @return the legend of the trend chart
     */
    public String getTrendName() {
        return Messages.Tool_Trend_Name(getName());
    }

    /**
     * Returns the absolute URL to the small icon for the tool.
     *
     * @return absolute URL
     */
    public String getSmallIconUrl() {
        return icon;
    }

    /**
     * Returns the absolute URL to the large icon for the tool.
     *
     * @return absolute URL
     */
    public String getLargeIconUrl() {
        return icon;
    }

    /**
     * Returns a short description describing the total number of issues.
     *
     * @param numberOfItems
     *         the number of issues to report
     *
     * @return the description
     */
    public String getToolTip(final int numberOfItems) {
        return getToolTipLocalizable(numberOfItems).toString();
    }

    /**
     * Returns a short description describing the total number of issues.
     *
     * @param numberOfItems
     *         the number of issues to report
     *
     * @return the description
     */
    Localizable getToolTipLocalizable(final int numberOfItems) {
        return new CompositeLocalizable(getName(), createToolTipSuffix(numberOfItems));
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private Localizable createToolTipSuffix(final int numberOfItems) {
        if (numberOfItems == 0) {
            return Messages._Tool_NoIssues();
        }
        if (numberOfItems == 1) {
            return Messages._Tool_OneIssue();
        }
        return Messages._Tool_MultipleIssues(numberOfItems);
    }

    @Override
    public String getDescription(final Issue issue) {
        return descriptionProvider.getDescription(issue);
    }

    /**
     * Returns an additional description of the specified issue that will be shown with the source code.
     *
     * @param build
     *         the current build
     * @param issue
     *         the issue
     *
     * @return the additional description
     */
    public String getSourceCodeDescription(final Run<?, ?> build, final Issue issue) {
        return getDescription(issue);
    }

    /**
     * Functional interface that maps the age of a build from an integer value to a String value.
     */
    public interface AgeBuilder extends Function<Integer, String> {
        // no new methods
    }

    /**
     * Computes the age of a build as a hyperlink.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static class DefaultAgeBuilder implements AgeBuilder {
        private final int currentBuildNumber;
        private final String resultUrl;
        @CheckForNull
        private Job<?, ?> owner;

        /**
         * Creates a new instance of {@link DefaultAgeBuilder}.
         *
         * @param currentBuildNumber
         *         number of the current build
         * @param resultUrl
         *         URL to the results
         * @deprecated use {@link #DefaultAgeBuilder(int, String, Job)}
         */
        @Deprecated
        public DefaultAgeBuilder(final int currentBuildNumber, final String resultUrl) {
            this.currentBuildNumber = currentBuildNumber;
            this.resultUrl = resultUrl;
        }

        /**
         * Creates a new instance of {@link DefaultAgeBuilder}.
         *
         * @param currentBuildNumber
         *         number of the current build
         * @param resultUrl
         *         URL to the results
         * @param job
         *         the job
         */
        public DefaultAgeBuilder(final int currentBuildNumber, final String resultUrl, final Job<?, ?> job) {
            this(currentBuildNumber, resultUrl);

            this.owner = job;
        }

        @Override
        public String apply(final Integer referenceBuild) {
            if (referenceBuild >= currentBuildNumber || referenceBuild <= 0) {
                return "1"; // fallback
            }
            var referenceBuildId = String.valueOf(referenceBuild);
            if (owner != null && owner.getBuild(referenceBuildId) == null) {
                return computeAge(referenceBuild); // plain link
            }
            else {
                String cleanUrl = StringUtils.stripEnd(resultUrl, "/");
                int subDetailsCount = StringUtils.countMatches(cleanUrl, "/");
                String backward = StringUtils.repeat("../", subDetailsCount + 2);
                String detailsUrl = StringUtils.substringBefore(cleanUrl, "/");

                String url = String.format("%s%d/%s", backward, referenceBuild, detailsUrl);
                return a(computeAge(referenceBuild))
                        .withHref(StringUtils.stripEnd(url, "/")).render();
            }
        }

        private String computeAge(final int buildNumber) {
            return String.valueOf(currentBuildNumber - buildNumber + 1);
        }
    }

    /**
     * Creates a {@link Localizable} that is composed of a prefix and suffix.
     */
    static class CompositeLocalizable extends Localizable {
        private static final long serialVersionUID = 2819361593374249688L;

        private final String prefix;
        private final Localizable suffix;

        CompositeLocalizable(final String prefix, final Localizable suffix) {
            super(null, suffix.getKey());
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        public String toString(final Locale locale) {
            return String.format("%s: %s", prefix, suffix);
        }
    }
}
