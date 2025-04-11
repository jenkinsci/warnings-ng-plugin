package io.jenkins.plugins.analysis.core.tokens;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;

/**
 * Provides a token that evaluates to the number of issues.
 *
 * @author Ullrich Hafner
 */
@Extension(optional = true)
public class IssuesSizeTokenMacro extends DataBoundTokenMacro {
    private String tool;
    private StatisticProperties type = StatisticProperties.TOTAL;

    @Parameter
    public void setTool(final String tool) {
        this.tool = tool;
    }

    /**
     * Defines which value should be computed in the macro.
     *
     * @param type
     *         the type to show
     */
    @Parameter
    public void setType(final String type) {
        this.type = StatisticProperties.valueOf(type);
    }

    @Override
    public boolean acceptsMacroName(final String macroName) {
        return "ANALYSIS_ISSUES_COUNT".equals(macroName);
    }

    @Override
    public String evaluate(final AbstractBuild<?, ?> abstractBuild, final TaskListener taskListener,
            final String macroName) {
        return extractSelectedTotals(abstractBuild);
    }

    @Override
    public String evaluate(final Run<?, ?> run, final FilePath workspace, final TaskListener listener,
            final String macroName) {
        return extractSelectedTotals(run);
    }

    private String extractSelectedTotals(final Run<?, ?> run) {
        return String.valueOf(run.getActions(ResultAction.class).stream()
                .filter(createToolFilter())
                .map(ResultAction::getResult)
                .map(AnalysisResult::getTotals)
                .mapToInt(totals -> type.getSizeGetter().apply(totals))
                .reduce(Integer::sum)
                .orElse(0));
    }

    private Predicate<ResultAction> createToolFilter() {
        if (StringUtils.isBlank(tool)) {
            return jobAction -> true;
        }
        else {
            return jobAction -> jobAction.getId().equals(tool);
        }
    }
}
