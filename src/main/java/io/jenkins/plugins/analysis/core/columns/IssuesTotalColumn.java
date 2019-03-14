package io.jenkins.plugins.analysis.core.columns;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.Extension;
import hudson.model.Job;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.ToolSelection;

import static io.jenkins.plugins.analysis.core.model.ToolSelection.*;

/**
 * Shows the number of issues of a job in a column of a Jenkins view. This column provides an auto-selection mode that
 * selects all tools that are available for a job. If you are interested in individual results you can also select the
 * participating tools one by one.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.DataClass")
public class IssuesTotalColumn extends ListViewColumn {
    private boolean selectTools = false;
    private List<ToolSelection> tools = new ArrayList<>();
    private String name = "# Issues";

    private LabelProviderFactory labelProviderFactory = new LabelProviderFactory();

    /** Creates a new instance of {@link ToolSelection}. */
    @DataBoundConstructor
    public IssuesTotalColumn() {
        super();
        // empty constructor required for stapler
    }

    @SuppressWarnings("unused") // called by Stapler
    public boolean getSelectTools() {
        return selectTools;
    }

    /**
     * Determines whether all available tools should be selected or if the selection should be done individually.
     *
     * @param selectTools
     *         if {@code true} the selection of tools can be done manually by selecting the corresponding ID, otherwise
     *         all available tools in a job are automatically selected
     */
    @SuppressWarnings("WeakerAccess") // called by Stapler
    @DataBoundSetter
    public void setSelectTools(final boolean selectTools) {
        this.selectTools = selectTools;
    }

    public List<ToolSelection> getTools() {
        return tools;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the column.
     *
     * @param name
     *         the name of the column
     */
    @DataBoundSetter
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the tools that should be taken into account when summing up the totals of a job.
     *
     * @param tools
     *         the tools to select
     *
     * @see #setSelectTools(boolean)
     */
    @DataBoundSetter
    public void setTools(final List<ToolSelection> tools) {
        this.tools = tools;
    }

    @VisibleForTesting
    void setLabelProviderFactory(final LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = labelProviderFactory;
    }

    private LabelProviderFactory getLabelProviderFactory() {
        return ObjectUtils.defaultIfNull(labelProviderFactory, new LabelProviderFactory());
    }

    /**
     * Returns the total number of issues for the selected static analysis tool in a given job.
     *
     * @param job
     *         the job to select
     *
     * @return the number of issues for a tool in a given job
     */
    @SuppressWarnings("WeakerAccess") // called bv view
    public OptionalInt getTotal(final Job<?, ?> job) {
        return job.getActions(JobAction.class).stream()
                .filter(createToolFilter(selectTools, tools))
                .map(JobAction::getLatestAction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ResultAction::getResult)
                .mapToInt(AnalysisResult::getTotalSize)
                .reduce(Integer::sum);
    }

    /**
     * Returns the total number of issues for the selected static analysis tool in a given job.
     *
     * @param job
     *         the job to select
     *
     * @return the number of issues for a tool in a given job
     */
    @SuppressWarnings("WeakerAccess") // called bv view
    public List<AnalysisResultDescription> getDetails(final Job<?, ?> job) {
        return job.getActions(JobAction.class).stream()
                .filter(createToolFilter(selectTools, tools))
                .map(JobAction::getLatestAction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(result -> new AnalysisResultDescription(result, getLabelProviderFactory()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the URL to the selected static analysis results, if unique.
     *
     * @param job
     *         the job to select
     *
     * @return the URL to the results, if this column renders the results of a unique tool, empty string otherwise
     */
    public String getUrl(final Job<?, ?> job) {
        Set<String> actualIds = job.getActions(JobAction.class)
                .stream()
                .map(JobAction::getId)
                .collect(Collectors.toSet());

        String[] selectedIds = getIds(tools);
        if (selectedIds.length == 1) {
            String url = selectedIds[0];
            if (actualIds.contains(url)) {
                return url;
            }
        }

        if (actualIds.size() == 1) {
            return actualIds.iterator().next();
        }

        return StringUtils.EMPTY;
    }

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    @Extension(optional = true)
    public static class IssuesTablePortletDescriptor extends ListViewColumnDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.IssuesTotalColumn_Name();
        }
    }

    /**
     * Model for one {@link AnalysisResult} in a job.
     */
    public static class AnalysisResultDescription {
        private final String icon;
        private final String name;
        private final int total;
        private final String url;

        @VisibleForTesting
        AnalysisResultDescription(final String icon, final String name, final int total, final String url) {
            this.icon = icon;
            this.name = name;
            this.total = total;
            this.url = url;
        }

        AnalysisResultDescription(final ResultAction result, final LabelProviderFactory labelProviderFactory) {
            StaticAnalysisLabelProvider labelProvider = labelProviderFactory.create(result.getId(), result.getName());
            this.name = labelProvider.getLinkName();
            this.icon = labelProvider.getSmallIconUrl();
            total = result.getResult().getTotalSize();
            url = result.getUrlName();
        }

        public String getIcon() {
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public int getTotal() {
            return total;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AnalysisResultDescription that = (AnalysisResultDescription) o;
            return total == that.total
                    && Objects.equals(icon, that.icon)
                    && Objects.equals(name, that.name)
                    && Objects.equals(url, that.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(icon, name, total, url);
        }

        @Override
        public String toString() {
            return "AnalysisResultDescription{"
                    + "icon='" + icon + '\''
                    + ", name='" + name + '\''
                    + ", total=" + total
                    + ", url='" + url + '\''
                    + '}';
        }
    }
}
