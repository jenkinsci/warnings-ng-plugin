package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.AnnotationProvider;
import hudson.plugins.warnings.util.model.DefaultAnnotationContainer;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Base class for annotation detail objects. Instances of this class could be used for
 * Hudson Stapler objects that contain a subset of annotations.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractAnnotationsDetail extends AnnotationContainer implements ModelObject {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 1750266351592937774L;
    /** Current build as owner of this object. */
    private final AbstractBuild<?, ?> owner;

    /**
     * Creates a new instance of <code>AbstractWarningsDetail</code>.
     *
     * @param owner
     *            current build as owner of this object.
     * @param annotations
     *            the set of warnings represented by this object
     * @param name
     *            the name of this object
     */
    public AbstractAnnotationsDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations, final String name, final Hierarchy hierarchy) {
        super(name, hierarchy);
        this.owner = owner;

        addAnnotations(annotations);
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    public String getHeader() {
        return getName() + " - " + getDisplayName();
    }

    /**
     * Returns the build as owner of this object.
     *
     * @return the owner
     */
    public final AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this build is the last available build.
     *
     * @return <code>true</code> if this build is the last available build
     */
    public final boolean isCurrent() {
        return owner.getProject().getLastBuild().number == owner.number;
    }

    /**
     * Creates a detail graph for the specified detail object.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @param detailObject
     *            the detail object to compute the graph for
     * @param upperBound
     *            the upper bound of all tasks
     * @throws IOException
     *             in case of an error
     */
    protected final void createDetailGraph(final StaplerRequest request, final StaplerResponse response,
            final AnnotationProvider detailObject, final int upperBound) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        JFreeChart chart = ChartBuilder.createHighNormalLowChart(
                detailObject.getNumberOfAnnotations(Priority.HIGH),
                detailObject.getNumberOfAnnotations(Priority.NORMAL),
                detailObject.getNumberOfAnnotations(Priority.LOW), upperBound);
        ChartUtil.generateGraph(request, response, chart, 400, 20);
    }

    /**
     * Returns a localized priority name.
     *
     * @param priorityName
     *            priority as String value
     * @return localized priority name
     */
    public String getLocalizedPriority(final String priorityName) {
        return Priority.fromString(priorityName).getLongLocalizedString();
    }

    /**
     * Returns the dynamic result of this module detail view. Depending on the
     * number of packages, one of the following detail objects is returned:
     * <ul>
     * <li>A detail object for a single workspace file (if the module contains
     * only one package).</li>
     * <li>A package detail object for a specified package (in any other case).</li>
     * </ul>
     *
     * @param link
     *            the link to identify the sub page to show
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of this module detail view
     */
    public final Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        PriorityDetailFactory factory = new PriorityDetailFactory();
        if (factory.isPriority(link)) {
            return factory.create(link, owner, this, getDisplayName());
        }
        else if (link.startsWith("module.")) {
            return new ModuleDetail(getOwner(), getModule(StringUtils.substringAfter(link, "module.")), getDisplayName());
        }
        else if (link.startsWith("package.")) {
            return new PackageDetail(getOwner(), getPackage(StringUtils.substringAfter(link, "package.")), getDisplayName());
        }
        else if (link.startsWith("source.")) {
            return new SourceDetail(getOwner(), getAnnotation(StringUtils.substringAfter(link, "source.")));
        }
        else if (link.startsWith("category.")) {
            String category = StringUtils.substringAfter(link, "category.");
            return new AttributeDetail(getOwner(), getCategory(category), getDisplayName(), Messages.CategoryDetail_header() + " " + category);
        }
        else if (link.startsWith("type.")) {
            String type = StringUtils.substringAfter(link, "type.");
            return new AttributeDetail(getOwner(), getType(type), getDisplayName(), Messages.TypeDetail_header() + " " + type);
        }
        return null;
    }

    /**
     * Generates a PNG image for high/normal/low distribution of the specified object.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doStatistics(final StaplerRequest request, final StaplerResponse response) throws IOException {
        String parameter = request.getParameter("object");
        if (parameter.startsWith("category.")) {
            Set<FileAnnotation> annotations = getCategory(StringUtils.substringAfter(parameter, "category."));
            ChartRenderer.renderPriorititesChart(request, response, new DefaultAnnotationContainer(annotations), getAnnotationBound());
        }
        else if (parameter.startsWith("type.")) {
            Set<FileAnnotation> annotations = getType(StringUtils.substringAfter(parameter, "type."));
            ChartRenderer.renderPriorititesChart(request, response, new DefaultAnnotationContainer(annotations), getAnnotationBound());
        }
        else if (parameter.startsWith("package.")) {
            AnnotationContainer annotations = getPackage(StringUtils.substringAfter(parameter, "package."));
            ChartRenderer.renderPriorititesChart(request, response, annotations, getAnnotationBound());
        }
        else if (parameter.startsWith("module.")) {
            AnnotationContainer annotations = getModule(StringUtils.substringAfter(parameter, "module."));
            ChartRenderer.renderPriorititesChart(request, response, annotations, getAnnotationBound());
        }
    }

    /**
     * Returns all possible priorities.
     *
     * @return all priorities
     */
    public Priority[] getPriorities() {
        return Priority.values();
    }
}
