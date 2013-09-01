package hudson.plugins.analysis.views;

import hudson.model.Job;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.HtmlPrinter;
import hudson.plugins.analysis.util.model.Priority;

import hudson.views.ListViewColumn;

/**
 * A column that shows the total number of warnings in a job.
 *
 * @author Ulli Hafner
 * @param <T>
 *            project action type to extract the warning counts from
 */
public abstract class WarningsCountColumn<T extends AbstractProjectAction<?>> extends ListViewColumn {
    private static final String NO_RESULT = "-";

    /**
     * Returns the URL of the referenced project action for the selected job.
     *
     * @param project
     *            the selected project
     * @return the URL of the project action
     */
    public String getUrl(final Job<?, ?> project) {
        T action = getProjectAction(project);

        if (action == null) {
            return null;
        }
        else {
            return project.getUrl() + action.getUrlName();
        }
    }

    /**
     * Returns whether a link can be shown that shows the results of the referenced project action for the selected job.
     *
     * @param project
     *            the selected project
     * @return the URL of the project action
     */
    public boolean hasUrl(final Job<?, ?> project) {
        String numberOfAnnotations = getNumberOfAnnotations(project);

        return !(NO_RESULT.equals(numberOfAnnotations) || "0".equals(numberOfAnnotations));
    }

    /**
     * Returns the total number of annotations for the selected job.
     *
     * @param project
     *            the selected project
     * @return the total number of annotations
     */
    public String getNumberOfAnnotations(final Job<?, ?> project) {
        T action = getProjectAction(project);

        if (action != null && action.hasValidResults()) {
            return String.valueOf(getResult(action).getNumberOfAnnotations());
        }
        else {
            return NO_RESULT;
        }
    }

    private T getProjectAction(final Job<?, ?> project) {
        return project.getAction(getProjectAction());
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP")
    private BuildResult getResult(final T action) {
        return action.getLastAction().getResult();
    }

    /**
     * Returns the project action that contains the results.
     *
     * @return the project action
     */
    protected abstract Class<T> getProjectAction();

    /**
     * Returns the number of warnings for the specified job separated by each plug-in.
     *
     * @param project
     *            the job to get the warnings for
     * @return the number of warnings, formatted as HTML string
     */
    public String getDetails(final Job<?, ?> project) {
        HtmlPrinter printer = new HtmlPrinter();
        printer.append("<table>");
        T action = getProjectAction(project);
        if (action != null && action.hasValidResults()) {
            BuildResult result = getResult(action);
            if (result.isSuccessfulTouched()) {
                printer.append(printer.line(Messages.ResultAction_Status() + result.getResultIcon()));
            }
            if (result.getNumberOfNewWarnings() > 0) {
                print(printer, Messages.NewWarningsDetail_Name(), result.getNumberOfNewWarnings());
            }

            print(printer, Priority.HIGH, result.getNumberOfHighPriorityWarnings());
            print(printer, Priority.NORMAL, result.getNumberOfNormalPriorityWarnings());
            print(printer, Priority.LOW, result.getNumberOfLowPriorityWarnings());
        }
        else {
            return Messages.Column_NoResults();
        }
        printer.append("</table>");
        return printer.toString();
    }

    private void print(final HtmlPrinter printer, final Priority priority, final int count) {
        print(printer, priority.getLocalizedString(), count);
    }

    private void print(final HtmlPrinter printer, final String label, final int count) {
        printer.append(printer.line(label + ": " + count));
    }
}
