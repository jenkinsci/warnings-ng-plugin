package io.jenkins.plugins.analysis.core.model;

import java.util.Locale;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.UnescapedText;

import org.jvnet.localizer.Localizable;
import hudson.model.BallColor;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.fontawesome.api.SvgTag;
import io.jenkins.plugins.util.JenkinsFacade;

import static j2html.TagCreator.*;

/**
 * A generic label provider for static analysis results. Creates pre-defined labels that are parameterized with a string
 * placeholder, that will be replaced with the actual name of the static analysis tool. Moreover, such a default label
 * provider decorates the links and summary boxes with the default icon of the warnings plug-in.
 *
 * @author Ullrich Hafner
 */
public class StaticAnalysisLabelProvider implements DescriptionProvider {
    private static final String ICONS_PREFIX = "/plugin/warnings-ng/icons/";
    private static final String ANALYSIS_SVG_ICON = ICONS_PREFIX + "analysis.svg";

    @VisibleForTesting
    static final String ERROR_ICON = "exclamation-triangle";
    @VisibleForTesting
    static final String INFO_ICON = "info-circle";
    /** Provides an empty description. */
    protected static final DescriptionProvider EMPTY_DESCRIPTION = i -> StringUtils.EMPTY;

    private final String id;
    @CheckForNull
    private String name;
    private final DescriptionProvider descriptionProvider;
    private final JenkinsFacade jenkins;

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
        this(id, name, EMPTY_DESCRIPTION, new JenkinsFacade());
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
        this(id, name, descriptionProvider, new JenkinsFacade());
    }

    @VisibleForTesting
    StaticAnalysisLabelProvider(final String id, @CheckForNull final String name,
            final DescriptionProvider descriptionProvider, final JenkinsFacade jenkins) {
        this.id = id;
        this.descriptionProvider = descriptionProvider;
        this.jenkins = jenkins;

        changeName(name);
    }

    private void changeName(final String originalName) {
        if (StringUtils.isNotBlank(originalName) && !"-".equals(originalName)) { // don't overwrite with empty or -
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
        return new DefaultAgeBuilder(owner.getNumber(), url);
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
    public String toString() {
        return String.format("%s: %s", getId(), getName());
    }

    /**
     * Returns the name of the link to the results.
     *
     * @return the name of the side panel link
     */
    public String getLinkName() {
        return getRawLinkName();
    }

    /**
     * Returns the name of the link to the results.
     *
     * @return the name of the side panel link
     * @deprecated use {@link #getLinkName()}
     */
    @Deprecated
    public String getRawLinkName() {
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
        return ANALYSIS_SVG_ICON;
    }

    /**
     * Returns the absolute URL to the large icon for the tool.
     *
     * @return absolute URL
     */
    public String getLargeIconUrl() {
        return ANALYSIS_SVG_ICON;
    }

    /**
     * Returns the title for the small information box in the corresponding build page.
     *
     * @param result
     *         the result
     * @param hasErrors
     *         indicates if an error has been reported
     *
     * @return the title div
     * @deprecated rendering of the summary is now done on the client side with the new model {@link SummaryModel}
     */
    @Deprecated
    public ContainerTag getTitle(final AnalysisResult result, final boolean hasErrors) {
        String icon = hasErrors ? ERROR_ICON : INFO_ICON;
        return span(join(getName() + ": ",
                getWarningsCount(result),
                a().withHref(getId() + "/info")
                        .with(new UnescapedText(new SvgTag(icon, jenkins)
                                .withClasses("info-page-decorator")
                                .render())))).withId(id + "-title");
    }

    /**
     * Returns the HTML label for the link to the new issues of the build.
     *
     * @param newSize
     *         the number of new issues
     *
     * @return the legend of the trend chart
     * @deprecated rendering of the summary is now done on the client side with the new model {@link SummaryModel}
     */
    @Deprecated
    public ContainerTag getNewIssuesLabel(final int newSize) {
        return a(newSize == 1 ? Messages.Tool_OneNewWarning() : Messages.Tool_MultipleNewWarnings(newSize))
                .withHref(getId() + "/new");
    }

    /**
     * Returns the HTML label for the link to the fixed issues of the build.
     *
     * @param fixedSize
     *         the number of fixed issues
     *
     * @return the legend of the trend chart
     * @deprecated rendering of the summary is now done on the client side with the new model {@link SummaryModel}
     */
    @Deprecated
    public ContainerTag getFixedIssuesLabel(final int fixedSize) {
        return a(fixedSize == 1 ? Messages.Tool_OneFixedWarning() : Messages.Tool_MultipleFixedWarnings(fixedSize))
                .withHref(getId() + "/fixed");
    }

    /**
     * Returns the HTML text showing the number of builds since the project has no issues.
     *
     * @param currentBuild
     *         the current build number
     * @param noIssuesSinceBuild
     *         the build since there are no issues
     *
     * @return the legend of the trend chart
     * @deprecated rendering of the summary is now done on the client side with the new model {@link SummaryModel}
     */
    @Deprecated
    public DomContent getNoIssuesSinceLabel(final int currentBuild, final int noIssuesSinceBuild) {
        return join(Messages.Tool_NoIssuesSinceBuild(Messages.Tool_NoIssues(),
                currentBuild - noIssuesSinceBuild + 1,
                a(String.valueOf(noIssuesSinceBuild))
                        .withHref("../" + noIssuesSinceBuild)
                        .withClasses("model-link", "inside").render()));
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private Object getWarningsCount(final AnalysisResult analysisRun) {
        int size = analysisRun.getTotalSize();
        if (size == 0) {
            return Messages.Tool_NoIssues();
        }
        if (size == 1) {
            return linkToIssues(Messages.Tool_OneIssue());
        }
        return linkToIssues(Messages.Tool_MultipleIssues(size));
    }

    private ContainerTag linkToIssues(final String linkText) {
        return a(linkText).withHref(getId());
    }

    /**
     * Returns the HTML text showing the result of the quality gate.
     *
     * @param qualityGateStatus
     *         the status of the quality gate
     *
     * @return the legend of the trend chart
     * @deprecated rendering of the summary is now done on the client side with the new model {@link SummaryModel}
     */
    @Deprecated
    public DomContent getQualityGateResult(final QualityGateStatus qualityGateStatus) {
        return getQualityGateResult(qualityGateStatus, true);
    }

    /**
     * Returns the HTML text showing the result of the quality gate.
     *
     * @param qualityGateStatus
     *         the status of the quality gate
     * @param hasResetLink
     *         determines whether the reset reference link is shown
     *
     * @return the legend of the trend chart
     * @deprecated rendering of the summary is now done on the client side with the new model {@link SummaryModel}
     */
    @Deprecated
    public DomContent getQualityGateResult(final QualityGateStatus qualityGateStatus, final boolean hasResetLink) {
        if (hasResetLink) {
            return join(Messages.Tool_QualityGate(), getResultIcon(qualityGateStatus),
                    button("Reset quality gate")
                            .withId(getId() + "-resetReference")
                            .withType("button")
                            .withClasses("btn", "btn-outline-primary", "btn-sm"));
        }
        return join(Messages.Tool_QualityGate(), getResultIcon(qualityGateStatus));
    }

    /**
     * Returns the HTML text showing a link to the reference build.
     *
     * @param referenceBuild
     *         the reference build
     *
     * @return the legend of the trend chart
     * @deprecated rendering of the summary is now done on the client side with the new model {@link SummaryModel}
     */
    @Deprecated
    public DomContent getReferenceBuild(final Run<?, ?> referenceBuild) {
        return join(Messages.Tool_ReferenceBuild(), createReferenceBuildLink(referenceBuild));
    }

    private ContainerTag createReferenceBuildLink(final Run<?, ?> referenceBuild) {
        String absoluteUrl = jenkins.getAbsoluteUrl(referenceBuild.getUrl(), getId());
        return a(referenceBuild.getFullDisplayName()).withHref(absoluteUrl);
    }

    private UnescapedText getResultIcon(final QualityGateStatus qualityGateStatus) {
        BallColor color = qualityGateStatus.getColor();
        return join(img().withSrc(jenkins.getImagePath(color))
                        .withClasses(color.getIconClassName())
                        .withAlt(color.getDescription())
                        .withTitle(color.getDescription()),
                color.getDescription());
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
     * Computes the age of a build as a hyper link.
     */
    public static class DefaultAgeBuilder implements AgeBuilder {
        private final int currentBuild;
        private final String resultUrl;

        /**
         * Creates a new instance of {@link DefaultAgeBuilder}.
         *
         * @param currentBuild
         *         number of the current build
         * @param resultUrl
         *         URL to the results
         */
        public DefaultAgeBuilder(final int currentBuild, final String resultUrl) {
            this.currentBuild = currentBuild;
            this.resultUrl = resultUrl;
        }

        @Override
        public String apply(final Integer referenceBuild) {
            if (referenceBuild >= currentBuild) {
                return "1"; // fallback
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
            return String.valueOf(currentBuild - buildNumber + 1);
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
