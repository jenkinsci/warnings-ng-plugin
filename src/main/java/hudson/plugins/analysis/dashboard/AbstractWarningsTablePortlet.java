package hudson.plugins.analysis.dashboard;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import hudson.model.Job;

import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.model.Priority;

/**
 * A portlet that shows a table with the number of warnings in the selected jobs.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractWarningsTablePortlet extends AbstractPortlet {
    private static final String CLOSE_TAG = ">";
    private static final String OPEN_TAG = "<";

    /** Message to be shown if no result action is found. */
    private static final String NO_RESULTS_FOUND = "-";

    /**
     * Creates a new instance of {@link AbstractWarningsTablePortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    public AbstractWarningsTablePortlet(final String name) {
        super(name);
    }

    /**
     * Returns the total number of warnings for the specified job.
     *
     * @param job
     *            the job to get the warnings for
     * @return the number of compiler warnings
     */
    public String getWarnings(final Job<?, ?> job) {
        return getWarnings(job, getAction(), getPluginName());
    }

    /**
     * Returns the total number of warnings for the specified job.
     *
     * @param job
     *            the job to get the warnings for
     * @param priority
     *            the priority
     * @return the number of compiler warnings
     */
    public String getWarnings(final Job<?, ?> job, final String priority) {
        return getWarnings(job, getAction(), Priority.valueOf(priority));
    }

    /**
     * Returns the number of compiler warnings for the specified jobs.
     *
     * @param jobs
     *            the jobs to get the warnings for
     * @return the number of compiler warnings
     */
    public String getWarnings(final Collection<Job<?, ?>> jobs) {
        int sum = 0;
        for (Job<?, ?> job : jobs) {
            sum += toInt(getWarnings(job));
        }
        return String.valueOf(sum);
    }

    /**
     * Returns the number of compiler warnings for the specified jobs.
     *
     * @param jobs
     *            the jobs to get the warnings for
     * @param priority
     *            the priority
     * @return the number of compiler warnings
     */
    public String getWarnings(final Collection<Job<?, ?>> jobs, final String priority) {
        int sum = 0;
        for (Job<?, ?> job : jobs) {
            sum += toInt(getWarnings(job, priority));
        }
        return String.valueOf(sum);
    }

    /**
     * Converts the string to an integer. If the string is not valid then 0
     * is returned.
     *
     * @param value
     *            the value to convert
     * @return the integer value or 0
     */
    private int toInt(final String value) {
        try {
            if (value.contains(OPEN_TAG)) {
                return Integer.parseInt(StringUtils.substringBetween(value, CLOSE_TAG, OPEN_TAG));
            }
            else {
                return Integer.parseInt(value);
            }
        }
        catch (NumberFormatException exception) {
            return 0;
        }
    }

    /**
     * Returns the warnings for the specified action.
     *
     * @param job
     *            the job to get the action from
     * @param actionType
     *            the type of the action
     * @param plugin
     *            the plug-in that is target of the link
     * @return the number of warnings
     */
    @SuppressWarnings("NP")
    private String getWarnings(final Job<?, ?> job, final Class<? extends AbstractProjectAction<?>> actionType, final String plugin) {
        AbstractProjectAction<?> action = job.getAction(actionType);
        if (action != null && action.hasValidResults()) {
            BuildResult result = action.getLastAction().getResult();
            int numberOfAnnotations = result.getNumberOfAnnotations();
            String value;
            if (numberOfAnnotations > 0) {
                value = String.format("<a href=\"%s%s\">%d</a>", job.getShortUrl(), plugin, numberOfAnnotations);
            }
            else {
                value = String.valueOf(numberOfAnnotations);
            }
            if (result.isSuccessfulTouched() && !result.isSuccessful()) {
                return value + result.getResultIcon();
            }
            return value;
        }
        return NO_RESULTS_FOUND;
    }

    /**
     * Returns the warnings for the specified action.
     *
     * @param job
     *            the job to get the action from
     * @param actionType
     *            the type of the action
     * @param priority
     *            priority of the warnings
     * @return the number of warnings
     */
    @SuppressWarnings("NP")
    private String getWarnings(final Job<?, ?> job, final Class<? extends AbstractProjectAction<?>> actionType, final Priority priority) {
        AbstractProjectAction<?> action = job.getAction(actionType);
        if (action != null && action.hasValidResults()) {
            BuildResult result = action.getLastAction().getResult();

            return String.valueOf(result.getNumberOfAnnotations(priority));
        }
        return NO_RESULTS_FOUND;
    }
}

