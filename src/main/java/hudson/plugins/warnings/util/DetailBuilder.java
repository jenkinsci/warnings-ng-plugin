package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.DefaultAnnotationContainer;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * Creates detail objects for the selected element of a annotation container.
 *
 * @author Ulli Hafner
 */
public class DetailBuilder {
    /** Default detail builder class. */
    private static final Class<DetailBuilder> DEFAULT_DETAIL_BUILDER = DetailBuilder.class;
    /** Detail builder class. */
    private static Class<? extends DetailBuilder> detailBuilder = DEFAULT_DETAIL_BUILDER;

    /**
     * Creates a new detail builder.
     *
     * @return the detail builder
     */
    public static DetailBuilder create() {
        try {
            return detailBuilder.newInstance();
        }
        catch (InstantiationException exception) {
            // ignore
        }
        catch (IllegalAccessException exception) {
            // ignore
        }
        return new DetailBuilder();
    }

    /**
     * Sets the detail builder class to the specified value.
     *
     * @param detailBuilder the value to set
     */
    public static void setDetailBuilder(final Class<? extends DetailBuilder> detailBuilder) {
        DetailBuilder.detailBuilder = detailBuilder;
    }

    /**
     * Returns a detail object for the selected element of the specified
     * annotation container. The details will include the new and fixed warnings
     * trends as well as the errors report.
     *
     * @param link
     *            the link to identify the sub page to show
     * @param owner
     *            the build as owner of the detail page
     * @param container
     *            the annotation container to get the details for
     * @param fixedAnnotations
     *            the annotations fixed in this build
     * @param newAnnotations
     *            the annotations new in this build
     * @param errors
     *            the errors in this build
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param displayName
     *            the name of the selected object
     * @return the dynamic result of this module detail view
     */
    // CHECKSTYLE:OFF
    public Object createTrendDetails(final String link, final AbstractBuild<?, ?> owner,
            final AnnotationContainer container, final Collection<FileAnnotation> fixedAnnotations,
            final Collection<FileAnnotation> newAnnotations, final Collection<String> errors,
            final String defaultEncoding, final String displayName) {
    // CHECKSTYLE:ON
        if ("fixed".equals(link)) {
            return new FixedWarningsDetail(owner, fixedAnnotations, defaultEncoding, displayName);
        }
        else if ("new".equals(link)) {
            return new NewWarningsDetail(owner, newAnnotations, defaultEncoding, displayName);
        }
        else if ("error".equals(link)) {
            return new ErrorDetail(owner, errors);
        }
        else if (link.startsWith("tab.new")) {
            return new TabDetail(owner, newAnnotations, "/tabview/new.jelly", defaultEncoding);
        }
        else if (link.startsWith("tab.fixed")) {
            return new TabDetail(owner, fixedAnnotations, "/tabview/fixed.jelly", defaultEncoding);
        }
        else {
            return createDetails(link, owner, container, defaultEncoding, displayName);
        }
    }

    /**
     * Returns a detail object for the selected element of the specified annotation container.
     *
     * @param link the link to identify the sub page to show
     * @param owner the build as owner of the detail page
     * @param container the annotation container to get the details for
     * @param defaultEncoding the default encoding to be used when reading and parsing files
     * @param displayName the name of the selected object
     *
     * @return the dynamic result of this module detail view
     */
    public Object createDetails(final String link, final AbstractBuild<?, ?> owner, final AnnotationContainer container,
            final String defaultEncoding, final String displayName) {
        PriorityDetailFactory factory = new PriorityDetailFactory();
        if (factory.isPriority(link)) {
            return factory.create(link, owner, container, defaultEncoding, displayName);
        }
        else if (link.startsWith("module.")) {
            return new ModuleDetail(owner, container.getModule(createHashCode(link, "module.")), defaultEncoding, displayName);
        }
        else if (link.startsWith("package.")) {
            return new PackageDetail(owner, container.getPackage(createHashCode(link, "package.")), defaultEncoding, displayName);
        }
        else if (link.startsWith("file.")) {
            return new FileDetail(owner, container.getFile(createHashCode(link, "file.")), defaultEncoding, displayName);
        }
        else if (link.startsWith("tab.")) {
            return new TabDetail(owner, container.getAnnotations(), "/tabview/" + StringUtils.substringAfter(link, "tab.") + ".jelly", defaultEncoding);
        }
        else if (link.startsWith("source.")) {
            return new SourceDetail(owner, container.getAnnotation(StringUtils.substringAfter(link, "source.")), defaultEncoding);
        }
        else if (link.startsWith("category.")) {
            DefaultAnnotationContainer category = container.getCategory(createHashCode(link, "category."));
            return new AttributeDetail(owner, category.getAnnotations(), defaultEncoding, displayName, Messages.CategoryDetail_header() + " " + category.getName());
        }
        else if (link.startsWith("type.")) {
            DefaultAnnotationContainer type = container.getType(createHashCode(link, "type."));
            return new AttributeDetail(owner, type.getAnnotations(), defaultEncoding, displayName, Messages.TypeDetail_header() + " " + type.getName());
        }
        return null;
    }


    /**
     * Extracts the hash code from the given link stripping of the given prefix.
     *
     * @param link the whole link
     * @param prefix the prefix to remove
     *
     * @return the hash code
     */
    private int createHashCode(final String link, final String prefix) {
        return Integer.parseInt(StringUtils.substringAfter(link, prefix));
    }

    /**
     * Creates a new instance of {@link DetailBuilder}.
     */
    protected DetailBuilder() {
        // make constructor protected
    }
}

