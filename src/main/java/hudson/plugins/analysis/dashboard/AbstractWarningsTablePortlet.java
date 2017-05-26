package hudson.plugins.analysis.dashboard;

import javax.annotation.CheckForNull;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

import hudson.model.Job;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

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
    private final boolean canHideZeroWarningsProjects;

    /**
     * Creates a new instance of {@link AbstractWarningsTablePortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    public AbstractWarningsTablePortlet(final String name) {
        this(name, false);
    }

    /**
     * Creates a new instance of {@link AbstractWarningsTablePortlet}.
     *
     * @param name
     *            the name of the portlet
     * @param canHideZeroWarningsProjects
     *            determines if zero warnings projects should be hidden in the
     *            table
     */
    public AbstractWarningsTablePortlet(final String name, final boolean canHideZeroWarningsProjects) {
        super(name);

        this.canHideZeroWarningsProjects = canHideZeroWarningsProjects;
    }

    /**
     * Returns whether zero warnings projects should be hidden in the table.
     *
     * @return <code>true</code> then only projects that contain warnings are
     *         shown, <code>false</code> all projects are shown
     */
    public boolean getCanHideZeroWarningsProjects() {
        return canHideZeroWarningsProjects;
    }

    /**
     * Returns all jobs that have warnings.
     *
     * @param jobs
     *            all jobs
     * @return the jobs with warnings
     */
    public Collection<Job<?, ?>> filterZeroWarningsJobs(final Collection<Job<?, ?>> jobs) {
        if (canHideZeroWarningsProjects) {
            return filter(jobs);
        }
        else {
            return jobs;
        }
    }

    /**
     * Filters the specified collection of jobs using overridable method
     * {@link #isVisibleJob(Job)}.
     *
     * @param jobs
     *            the jobs to filter
     * @return the filtered jobs
     * @see #isVisibleJob(Job) filter predicate
     */
    protected Collection<Job<?, ?>> filter(final Collection<Job<?, ?>> jobs) {
        List<Job<?, ?>> filtered = Lists.newArrayList();
        for (Job<?, ?> job : jobs) {
            if (isVisibleJob(job)) {
                filtered.add(job);
            }
        }
        return filtered;
    }

    /**
     * Returns whether the specified job is visible. This default implementation
     * returns true if there is at least one warning for the job.
     *
     * @param job
     *            the job to check
     * @return <code>true</code> if the job is visible, <code>false</code>
     *         otherwise
     */
    protected boolean isVisibleJob(final Job<?, ?> job) {
        return toInt(getWarnings(job)) > 0;
    }

    /**
     * Returns the name of the plug-in this portlet belongs to.
     *
     * @return the plug-in name
     * @deprecated is not used anymore, the URL is resolved from the actions
     */
    @Deprecated
    protected String getPluginName() {
        return StringUtils.EMPTY;
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
    protected int toInt(final String value) {
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
     * Returns the total number of warnings for the specified job.
     *
     * @param job
     *            the job to get the warnings for
     * @return the number of compiler warnings
     */
    public String getWarnings(final Job<?, ?> job) {
        AbstractProjectAction<?> action = selectAction(job);
        if (action != null) {
            ResultAction<?> lastAction = action.getLastAction();
            if (lastAction != null) {
                BuildResult result = lastAction.getResult();
                int numberOfAnnotations = result.getNumberOfAnnotations();
                String value;
                if (numberOfAnnotations > 0) {
                    String prefix = getDashboard().getUrl();
                    String jobUrl = job.getUrl().replaceFirst(prefix, StringUtils.EMPTY);
                    value = String.format("<a href=\"%s%s\">%d</a>", jobUrl, action.getUrlName(), numberOfAnnotations);
                }
                else {
                    value = String.valueOf(numberOfAnnotations);
                }
                if (result.isSuccessfulTouched() && !result.isSuccessful()) {
                    return value + result.getResultIcon();
                }
                return value;
            }
        }
        return NO_RESULTS_FOUND;
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
        AbstractProjectAction<?> action = selectAction(job);
        if (action != null) {
            ResultAction<?> lastAction = action.getLastAction();
            if (lastAction != null) {
                BuildResult result = lastAction.getResult();

                return String.valueOf(result.getNumberOfAnnotations(priority));
            }
        }
        return NO_RESULTS_FOUND;
    }

    /**
     * Selects the action to show the results from. This default implementation
     * simply returns the first action that matches the given type.
     *
     * @param job
     *            the job to get the action from
     * @return the action
     */
    @CheckForNull
    protected AbstractProjectAction<?> selectAction(final Job<?, ?> job) {
        if (job == null) {
            return null;
        }
        else {
            return job.getAction(getAction());
        }
    }
}

