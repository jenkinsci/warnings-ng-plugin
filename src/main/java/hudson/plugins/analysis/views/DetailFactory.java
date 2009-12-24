package hudson.plugins.analysis.views;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import hudson.model.AbstractBuild;
import hudson.model.Item;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Creates detail objects for the selected element of a annotation container.
 *
 * @author Ulli Hafner
 */
public class DetailFactory {
    /** Default detail builder class. */
    private static final Class<DetailFactory> DEFAULT_DETAIL_BUILDER = DetailFactory.class;
    /** Detail builder class. */
    private static Class<? extends DetailFactory> detailBuilder = DEFAULT_DETAIL_BUILDER;

    /**
     * Creates a new detail builder.
     *
     * @return the detail builder
     */
    public static DetailFactory create() {
        try {
            return detailBuilder.newInstance();
        }
        catch (InstantiationException exception) {
            // ignore
        }
        catch (IllegalAccessException exception) {
            // ignore
        }
        return new DetailFactory();
    }

    /**
     * Sets the detail builder class to the specified value.
     *
     * @param detailBuilder the value to set
     */
    public static void setDetailBuilder(final Class<? extends DetailFactory> detailBuilder) {
        DetailFactory.detailBuilder = detailBuilder;
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
            return createTabDetail(owner, newAnnotations, createGenericTabUrl(link), defaultEncoding);
        }
        else if (link.startsWith("tab.fixed")) {
            return createTabDetail(owner, fixedAnnotations, createGenericTabUrl(link), defaultEncoding);
        }
        else {
            return createDetails(link, owner, container, defaultEncoding, displayName);
        }
    }

    /**
     * Returns a detail object for the selected element of the specified
     * annotation container.
     *
     * @param link
     *            the link to identify the sub page to show
     * @param owner
     *            the build as owner of the detail page
     * @param container
     *            the annotation container to get the details for
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param displayName
     *            the name of the selected object
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
            return createTabDetail(owner, container.getAnnotations(), createGenericTabUrl(link), defaultEncoding);
        }
        else if (link.startsWith("source.")) {
            owner.checkPermission(Item.WORKSPACE);

            return new SourceDetail(owner, container.getAnnotation(StringUtils.substringAfter(link, "source.")), defaultEncoding);
        }
        else if (link.startsWith("category.")) {
            DefaultAnnotationContainer category = container.getCategory(createHashCode(link, "category."));
            return createAttributeDetail(owner, category, displayName, Messages.CategoryDetail_header(), defaultEncoding);
        }
        else if (link.startsWith("type.")) {
            DefaultAnnotationContainer type = container.getType(createHashCode(link, "type."));
            return createAttributeDetail(owner, type, displayName, Messages.TypeDetail_header(), defaultEncoding);
        }
        return null;
    }

    /**
     * Creates a generic detail tab with the specified link.
     *
     * @param owner
     *            the build as owner of the detail page
     * @param annotations
     *            the annotations to display
     * @param displayName
     *            the name of the view
     * @param header
     *            the bread crumb name
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @return the detail view
     */
    protected AttributeDetail createAttributeDetail(final AbstractBuild<?, ?> owner, final DefaultAnnotationContainer annotations,
            final String displayName, final String header, final String defaultEncoding) {
        return new AttributeDetail(owner, annotations.getAnnotations(), defaultEncoding, displayName, header + " " + annotations.getName());
    }

    /**
     * Creates a generic detail tab with the specified link.
     *
     * @param owner
     *            the build as owner of the detail page
     * @param annotations
     *            the annotations to display
     * @param url
     *            the URL for the details view
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @return the detail view
     */
    protected TabDetail createTabDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations,
            final String url, final String defaultEncoding) {
        return new TabDetail(owner, annotations, url, defaultEncoding);
    }

    /**
     * Creates the actual URL from the synthetic link.
     *
     * @param link
     *            the link
     * @return the actual URL
     */
    private String createGenericTabUrl(final String link) {
        return "/tabview/" + StringUtils.substringAfter(link, "tab.") + ".jelly";
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
     * Creates a new instance of {@link DetailFactory}.
     */
    protected DetailFactory() {
        // make constructor protected
    }
}

