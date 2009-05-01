package hudson.plugins.warnings.util;

import hudson.model.AbstractProject;
import hudson.model.ModelObject;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Configures the trend graph of this plug-in for the current user and job using a cookie.
 */
public class GraphConfigurationDetail extends GraphConfiguration implements ModelObject {
    /** The owning project to configure the graphs for. */
    private final AbstractProject<?, ?> project;
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(GraphConfigurationDetail.class.getName());
    /** Suffix of the cookie name that is used to persist the configuration per user. */
    private final String cookieName;
    /** The last result action to start the trend report computation from. */
    private ResultAction<?> lastAction;
    /** The health descriptor. */
    private AbstractHealthDescriptor healthDescriptor;

    /** Drawing Mode. */
    enum Mode {
        /** PNG image. */
        PNG,
        /** Clickable map for the PNG image. */
        MAP}

    /**
     * Creates a new instance of {@link GraphConfigurationDetail}.
     *
     * @param project
     *            the owning project to configure the graphs for
     * @param request
     *            the request with the optional cookie to initialize this
     *            instance
     * @param cookieName
     *            the suffix of the cookie name that is used to persist the
     *            configuration per user
     */
    public GraphConfigurationDetail(final AbstractProject<?, ?> project, final StaplerRequest request,
            final String cookieName) {
        super(createCookieHandler(cookieName).getValue(request.getCookies()));

        this.project = project;
        this.cookieName = cookieName;
        healthDescriptor = new NullHealthDescriptor();
    }

    /**
     * Creates a new instance of {@link GraphConfigurationDetail}.
     *
     * @param project
     *            the owning project to configure the graphs for
     * @param request
     *            the request with the optional cookie to initialize this
     *            instance
     * @param cookieName
     *            the suffix of the cookie name that is used to persist the
     *            configuration per user
     * @param lastAction the last valid action for this project
     */
    public GraphConfigurationDetail(final AbstractProject<?, ?> project, final StaplerRequest request,
            final String cookieName, final ResultAction<?> lastAction) {
        this(project, request, cookieName);
        this.lastAction = lastAction;
        healthDescriptor = lastAction.getHealthDescriptor();
    }

    /**
     * Creates a new cookie handler to convert the cookie to a string value.
     *
     * @param cookieName
     *            the suffix of the cookie name that is used to persist the
     *            configuration per user
     * @return the new cookie handler
     */
    private static CookieHandler createCookieHandler(final String cookieName) {
        return new CookieHandler(cookieName);
    }

    /**
     * Returns the project.
     *
     * @return the project
     */
    public AbstractProject<?, ?> getOwner() {
        return project;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.GraphConfiguration_Name();
    }

    /**
     * Returns the URL of this object.
     *
     * @return the URL of this object
     */
    public String getUrl() {
        return project.getAbsoluteUrl() + cookieName + "/configure";
    }

    /**
     * Saves the configured values to a cookie.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public void doSave(final StaplerRequest request, final StaplerResponse response) {
        try {
            JSONObject formData = request.getSubmittedForm();
            int width = formData.getInt("width");
            int height = formData.getInt("height");
            int buildCount = formData.getInt("buildCount");
            int dayCount = formData.getInt("dayCount");
            GraphType graphType = GraphType.valueOf(formData.getString("graphType"));

            if (isValid(width, height, buildCount, dayCount, graphType)) {
                String value = serializeToString(width, height, buildCount, dayCount, graphType);
                Cookie cookie = createCookieHandler(cookieName).create(request.getAncestors(), value);
                response.addCookie(cookie);
            }
        }
        catch (JSONException exception) {
            LOGGER.log(Level.SEVERE, "Can't parse the form data: " + request, exception);
        }
        catch (IllegalArgumentException exception) {
            LOGGER.log(Level.SEVERE, "Can't parse the form data: " + request, exception);
        }
        catch (ServletException exception) {
            LOGGER.log(Level.SEVERE, "Can't process the form data: " + request, exception);
        }
        finally {
            try {
                response.sendRedirect("../../");
            }
            catch (IOException exception) {
                LOGGER.log(Level.SEVERE, "Can't redirect", exception);
            }
        }
    }

    /**
     * Draws a PNG image with the new versus fixed warnings graph.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doNewVersusFixed(final StaplerRequest request, final StaplerResponse response) {
        drawNewVsFixed(request, response, Mode.PNG);
    }

    /**
     * Draws a MAP with the new versus fixed warnings graph.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doNewVersusFixedMap(final StaplerRequest request, final StaplerResponse response) {
        drawNewVsFixed(request, response, Mode.MAP);
    }

    /**
     * Draws new versus fixed warnings graph.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param mode
     *            drawing mode
     */
    private void drawNewVsFixed(final StaplerRequest request, final StaplerResponse response, final Mode mode) {
        if (lastAction != null) {
            JFreeChart graph = new NewVersusFixedGraph().create(this, lastAction, lastAction.getUrlName());
            generateGraph(request, response, graph, mode);
        }
    }

    /**
     * Draws a PNG image with a graph with warnings by priority.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doPriority(final StaplerRequest request, final StaplerResponse response) {
        drawPriority(request, response, Mode.PNG);
    }

    /**
     * Draws a MAP width a graph with warnings by priority.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doPriorityMap(final StaplerRequest request, final StaplerResponse response) {
        drawPriority(request, response, Mode.MAP);
    }

    /**
     * Draws a graph with warnings by priority.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param mode
     *            drawing mode
     */
    private void drawPriority(final StaplerRequest request, final StaplerResponse response, final Mode mode) {
        if (lastAction != null) {
            JFreeChart graph = new PriorityGraph().create(this, lastAction, lastAction.getUrlName());
            generateGraph(request, response, graph, mode);
        }
    }

    /**
     * Checks if the health graph is available.
     *
     * @return <code>true</code>, if the health graph is available
     */
    public boolean isHealthGraphAvailable() {
        return healthDescriptor.isEnabled();
    }

    /**
     * Draws a PNG image with a graph with warnings by health thresholds.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doHealth(final StaplerRequest request, final StaplerResponse response) {
        drawHealth(request, response, Mode.PNG);
    }

    /**
     * Draws a MAP with a graph with warnings by health thresholds.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     */
    public void doHealthMap(final StaplerRequest request, final StaplerResponse response) {
        drawHealth(request, response, Mode.MAP);
    }

    /**
     * Draws a graph with warnings by health thresholds.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param mode
     *            drawing mode
     */
    public void drawHealth(final StaplerRequest request, final StaplerResponse response, final Mode mode) {
        if (lastAction != null) {
            JFreeChart graph = new HealthGraph(healthDescriptor).create(this, lastAction, lastAction.getUrlName());
            generateGraph(request, response, graph, mode);
        }
    }


    /**
     * Generates the graph in PNG format and sends that to the response.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @param graph
     *            the graph
     * @param mode
     *            drawing mode
     */
    private void generateGraph(final StaplerRequest request, final StaplerResponse response, final JFreeChart graph, final Mode mode) {
        try {
            if (mode == Mode.PNG) {
                ChartUtil.generateGraph(request, response, graph, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            }
            else {
                ChartUtil.generateClickableMap(request, response, graph, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            }
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Can't create graph: " + request, exception);
        }
    }
}

