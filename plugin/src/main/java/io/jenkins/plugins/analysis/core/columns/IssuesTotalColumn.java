package io.jenkins.plugins.analysis.core.columns;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.verb.POST;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.util.ListBoxModel;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.ToolSelection;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;
import io.jenkins.plugins.util.JenkinsFacade;

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
    private boolean selectTools;
    private List<ToolSelection> tools = new ArrayList<>();
    private String name = "# Issues";

    private LabelProviderFactory labelProviderFactory = new LabelProviderFactory();
    private StatisticProperties type;

    /** Creates a new instance of {@link ToolSelection}. */
    @DataBoundConstructor
    public IssuesTotalColumn() {
        this(new JenkinsFacade());
    }

    @VisibleForTesting
    IssuesTotalColumn(final JenkinsFacade facade) {
        super();

        name = getConfiguration(facade).getDefaultName();
        type = getConfiguration(facade).getDefaultType();
    }

    /**
     * Called after deserialization to retain backward compatibility..
     *
     * @return this
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "Deserialization of instances that do not have all fields yet")
    protected Object readResolve() {
        if (type == null) {
            type = StatisticProperties.TOTAL;
        }

        return this;
    }

    @SuppressWarnings({"unused", "PMD.BooleanGetMethodName", "WeakerAccess"}) // called by Stapler
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
    // called by Stapler
    @DataBoundSetter
    public void setSelectTools(final boolean selectTools) {
        this.selectTools = selectTools;
    }

    public List<ToolSelection> getTools() {
        return tools;
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

    public StatisticProperties getType() {
        return type;
    }

    /**
     * Defines which value should be shown in the column.
     *
     * @param type
     *         the type of the values to show
     */
    @DataBoundSetter
    public void setType(final StatisticProperties type) {
        this.type = type;
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
        Run<?, ?> lastCompletedBuild = job.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return OptionalInt.empty();
        }

        return lastCompletedBuild.getActions(ResultAction.class).stream()
                .filter(createToolFilter(selectTools, tools))
                .map(ResultAction::getResult)
                .map(AnalysisResult::getTotals)
                .mapToInt(totals -> type.getSizeGetter().apply(totals))
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
        Run<?, ?> lastCompletedBuild = job.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return Collections.emptyList();
        }

        return lastCompletedBuild.getActions(ResultAction.class).stream()
                .filter(createToolFilter(selectTools, tools))
                .map(result -> new AnalysisResultDescription(result, getLabelProviderFactory(), type))
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
        Run<?, ?> lastCompletedBuild = job.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return StringUtils.EMPTY;
        }

        List<ResultAction> actions = lastCompletedBuild.getActions(ResultAction.class);
        Set<String> actualIds = actions.stream().map(ResultAction::getId).collect(Collectors.toSet());

        String[] selectedIds = getIds(tools);
        if (selectedIds.length == 1 && selectTools) {
            var selectedId = selectedIds[0];
            if (actualIds.contains(selectedId)) {
                //noinspection OptionalGetWithoutIsPresent
                var result = actions.stream().filter(action -> action.getId().equals(selectedId))
                        .findFirst().get(); // We are sure it contains the selected id
                return type.getUrl(result.getOwner().getNumber() + "/" + result.getUrlName());
            }
        }

        if (actualIds.size() == 1) {
            var result = actions.iterator().next();
            return type.getUrl(result.getOwner().getNumber() + "/" + result.getUrlName());
        }

        return StringUtils.EMPTY;
    }

    private static WarningsAppearanceConfiguration getConfiguration(final JenkinsFacade jenkins) {
        var configurations = jenkins.getDescriptorsFor(GlobalConfiguration.class);
        return Objects.requireNonNull(configurations.get(WarningsAppearanceConfiguration.class));
    }

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    @Extension(optional = true)
    @Symbol("issueTotalsColumn")
    public static class IssuesTablePortletDescriptor extends ListViewColumnDescriptor {
        private final JenkinsFacade jenkins;

        /**
         * Creates a new descriptor.
         */
        @SuppressWarnings("unused") // Required for Jenkins Extensions
        public IssuesTablePortletDescriptor() {
            this(new JenkinsFacade());
        }

        @VisibleForTesting
        IssuesTablePortletDescriptor(final JenkinsFacade jenkins) {
            super();

            this.jenkins = jenkins;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.IssuesTotalColumn_Name();
        }

        @Override
        public boolean shownByDefault() {
            return getConfiguration(jenkins).isEnableColumnByDefault();
        }

        /**
         * Return the model for the select widget.
         *
         * @return the quality gate types
         */
        @POST
        public ListBoxModel doFillTypeItems() {
            var model = new ListBoxModel();

            if (new JenkinsFacade().hasPermission(Jenkins.READ)) {
                for (StatisticProperties qualityGateType : StatisticProperties.values()) {
                    model.add(qualityGateType.getDisplayName(), qualityGateType.name());
                }
            }
            return model;
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

        AnalysisResultDescription(final ResultAction result, final LabelProviderFactory labelProviderFactory,
                final StatisticProperties type) {
            var labelProvider = labelProviderFactory.create(result.getId(), result.getName());
            name = labelProvider.getLinkName();
            icon = labelProvider.getSmallIconUrl();
            total = type.getSizeGetter().apply(result.getResult().getTotals());
            url = type.getUrl(result.getOwner().getNumber() + "/" + result.getUrlName());
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
            var that = (AnalysisResultDescription) o;
            return getTotal() == that.getTotal()
                    && Objects.equals(getIcon(), that.getIcon())
                    && Objects.equals(getName(), that.getName())
                    && Objects.equals(getUrl(), that.getUrl());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getIcon(), getName(), getTotal(), getUrl());
        }

        @Override
        public String toString() {
            return "AnalysisResultDescription{"
                    + "icon='" + getIcon() + '\''
                    + ", name='" + getName() + '\''
                    + ", total=" + getTotal()
                    + ", url='" + getUrl() + '\''
                    + '}';
        }
    }
}
