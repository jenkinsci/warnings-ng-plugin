package hudson.plugins.analysis.views;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;

import hudson.model.AbstractBuild;
import hudson.model.Item;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.Compatibility;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.AnnotationsLabelProvider;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.LineRange;

/**
 * Creates detail objects for the selected element of a annotation container.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("deprecation")
public class DetailFactory {
    /** Default detail builder class. */
    private static final DetailFactory DEFAULT_DETAIL_BUILDER = new DetailFactory();
    /** Maps plug-ins to detail builders. */
    private static Map<Class<? extends ResultAction<? extends BuildResult>>, DetailFactory> factories = Maps.newHashMap();

    /**
     * Creates a new detail builder.
     *
     * @param actionType
     *            the type of the action (i.e., the plug-in) to get the detail
     *            builder for
     * @return the detail builder
     */
    public static DetailFactory create(final Class<? extends ResultAction<? extends BuildResult>> actionType) {
        if (factories.containsKey(actionType)) {
            return factories.get(actionType);
        }
        return DEFAULT_DETAIL_BUILDER;
    }

    /**
     * Sets the detail builder class to the specified value.
     *
     * @param actionType
     *            the type of the action (i.e., the plug-in) to set the detail
     *            builder for
     * @param detailBuilder
     *            the value to set
     */
    public static void addDetailBuilder(final Class<? extends ResultAction<? extends BuildResult>> actionType,
            final DetailFactory detailBuilder) {
        synchronized (factories) {
            factories.put(actionType, detailBuilder);
        }
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
    @SuppressWarnings("deprecation")
    public Object createTrendDetails(final String link, @Nonnull final Run<?, ?> owner,
            final AnnotationContainer container, final Collection<FileAnnotation> fixedAnnotations,
            final Collection<FileAnnotation> newAnnotations, final Collection<String> errors,
            final String defaultEncoding, final String displayName) {
        // CHECKSTYLE:ON
        if (owner instanceof AbstractBuild && Compatibility.isOverridden(DetailFactory.class, getClass(), "createTrendDetails",
                String.class, AbstractBuild.class, AnnotationContainer.class, Collection.class, Collection.class, Collection.class, String.class, String.class)) {
            return createTrendDetails(link, (AbstractBuild<?, ?>) owner, container, fixedAnnotations, newAnnotations, errors, defaultEncoding, displayName);
        }
        else {
            AnnotationContainer detail;
            if ("fixed".equals(link)) {
                detail = createFixedWarningsDetail(owner, fixedAnnotations, defaultEncoding, displayName);
            }
            else if ("new".equals(link)) {
                detail = new NewWarningsDetail(owner, this, newAnnotations, defaultEncoding, displayName);
            }
            else if ("error".equals(link)) {
                return new ErrorDetail(owner, errors);
            }
            else if (link.startsWith("tab.new")) {
                detail = createTabDetail(owner, newAnnotations, createGenericTabUrl(link), defaultEncoding);
            }
            else if (link.startsWith("tab.fixed")) {
                detail = createTabDetail(owner, fixedAnnotations, createGenericTabUrl(link), defaultEncoding);
            }
            else {
                return createDetails(link, owner, container, defaultEncoding, displayName);
            }
            attachLabelProvider(detail);
            return detail;
        }
    }

    /**
     * Returns the default label provider that is used to visualize the build result (i.e., the tab labels).
     *
     * @return the default label probider
     * @since 1.69
     */
    protected void attachLabelProvider(final AnnotationContainer container) {
        container.setLabelProvider(new AnnotationsLabelProvider(container.getPackageCategoryTitle()));
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
    public Object createDetails(final String link, @Nonnull final Run<?, ?> owner, final AnnotationContainer container,
            final String defaultEncoding, final String displayName) {
        if (owner instanceof AbstractBuild && Compatibility.isOverridden(DetailFactory.class, getClass(), "createDetails",
                String.class, AbstractBuild.class, AnnotationContainer.class, String.class, String.class)) {
            return createDetails(link, (AbstractBuild<?, ?>) owner, container, defaultEncoding, displayName);
        }
        else {
            PriorityDetailFactory factory = new PriorityDetailFactory(this);
            AnnotationContainer detail = null;
            if (factory.isPriority(link)) {
                detail = factory.create(link, owner, container, defaultEncoding, displayName);
            }
            else if (link.startsWith("module.")) {
                detail = new ModuleDetail(owner, this, container.getModule(createHashCode(link, "module.")), defaultEncoding, displayName);
            }
            else if (link.startsWith("package.")) {
                detail = new PackageDetail(owner, this, container.getPackage(createHashCode(link, "package.")), defaultEncoding, displayName);
            }
            else if (link.startsWith("file.")) {
                detail = new FileDetail(owner, this, container.getFile(createHashCode(link, "file.")), defaultEncoding, displayName);
            }
            else if (link.startsWith("tab.")) {
                detail = createTabDetail(owner, container.getAnnotations(), createGenericTabUrl(link), defaultEncoding);
            }
            else if (link.startsWith("source.")) {
                owner.checkPermission(Item.WORKSPACE);

                FileAnnotation annotation = container.getAnnotation(StringUtils.substringAfter(link, "source."));
                if (annotation.isInConsoleLog()) {
                    LineRange lines = annotation.getLineRanges().iterator().next();
                    return new ConsoleDetail(owner, lines.getStart(), lines.getEnd());
                }
                else {
                    return new SourceDetail(owner, annotation, defaultEncoding);
                }
            }
            else if (link.startsWith("category.")) {
                DefaultAnnotationContainer category = container.getCategory(createHashCode(link, "category."));
                detail = createAttributeDetail(owner, category, displayName, Messages.CategoryDetail_header(), defaultEncoding);
            }
            else if (link.startsWith("type.")) {
                DefaultAnnotationContainer type = container.getType(createHashCode(link, "type."));
                detail = createAttributeDetail(owner, type, displayName, Messages.TypeDetail_header(), defaultEncoding);
            }
            if (detail != null) {
                attachLabelProvider(detail);
            }
            return detail;
        }
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
    protected AttributeDetail createAttributeDetail(@Nonnull final Run<?, ?> owner, final DefaultAnnotationContainer annotations,
            final String displayName, final String header, final String defaultEncoding) {
        if (owner instanceof AbstractBuild && Compatibility.isOverridden(DetailFactory.class, getClass(),
                "createAttributeDetail", AbstractBuild.class, DefaultAnnotationContainer.class, String.class, String.class, String.class)) {
            return createAttributeDetail((AbstractBuild<?, ?>) owner, annotations, displayName, header, defaultEncoding);
        }
        else {
            return new AttributeDetail(owner, this, annotations.getAnnotations(), defaultEncoding, displayName, header + " " + annotations.getName());
        }
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
    protected TabDetail createTabDetail(@Nonnull final Run<?, ?> owner, final Collection<FileAnnotation> annotations,
            final String url, final String defaultEncoding) {
        if (owner instanceof AbstractBuild && Compatibility.isOverridden(DetailFactory.class, getClass(), "createTabDetail", AbstractBuild.class,
                Collection.class, String.class, String.class)) {
            return createTabDetail((AbstractBuild<?, ?>) owner, annotations, url, defaultEncoding);
        }
        else {
            return new TabDetail(owner, this, annotations, url, defaultEncoding);
        }
    }

    /**
     * Creates a generic fixed warnings detail tab with the specified link.
     *
     * @param owner
     *            the build as owner of the detail page
     * @param fixedAnnotations
     *            the annotations to display
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param displayName
     *            the name of the view
     * @return the detail view
     */
    protected FixedWarningsDetail createFixedWarningsDetail(@Nonnull final Run<?, ?> owner,
            final Collection<FileAnnotation> fixedAnnotations, final String defaultEncoding,
            final String displayName) {
        if (owner instanceof AbstractBuild && Compatibility.isOverridden(DetailFactory.class, getClass(),
                "createFixedWarningsDetail", AbstractBuild.class, Collection.class, String.class, String.class)) {
            return createFixedWarningsDetail((AbstractBuild<?, ?>) owner, fixedAnnotations, defaultEncoding, displayName);
        }
        else {
            return new FixedWarningsDetail(owner, this, fixedAnnotations, defaultEncoding, displayName);
        }
    }

    /**
     * Creates the actual URL from the synthetic link.
     *
     * @param link
     *            the link
     * @return the actual URL
     */
    private String createGenericTabUrl(final String link) {
        return StringUtils.substringAfter(link, "tab.") + ".jelly";
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
     * @deprecated use {@link #createTabDetail(Run, Collection, String, String)} instead
     */
    @Deprecated
    protected TabDetail createTabDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations,
            final String url, final String defaultEncoding) {
        return new TabDetail(owner, this, annotations, url, defaultEncoding);
    }

    /**
     * Creates a generic fixed warnings detail tab with the specified link.
     *
     * @param owner
     *            the build as owner of the detail page
     * @param fixedAnnotations
     *            the annotations to display
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param displayName
     *            the name of the view
     * @return the detail view
     * @deprecated use {@link #createFixedWarningsDetail(Run, Collection, String, String)} instead
     */
    @Deprecated
    protected FixedWarningsDetail createFixedWarningsDetail(final AbstractBuild<?, ?> owner,
            final Collection<FileAnnotation> fixedAnnotations, final String defaultEncoding,
            final String displayName) {
        return new FixedWarningsDetail(owner, this, fixedAnnotations, defaultEncoding, displayName);
    }

    /**
     * @deprecated use {@link #createAttributeDetail(Run, DefaultAnnotationContainer, String, String, String)} instead
     */
    @Deprecated
    protected AttributeDetail createAttributeDetail(final AbstractBuild<?, ?> owner, final DefaultAnnotationContainer annotations,
            final String displayName, final String header, final String defaultEncoding) {
        return new AttributeDetail(owner, this, annotations.getAnnotations(), defaultEncoding, displayName, header + " " + annotations.getName());
    }

    /**
     * @deprecated use {@link #createTrendDetails(String, Run, AnnotationContainer, Collection, Collection, Collection, String, String)} instead
     */
    @Deprecated
    public Object createTrendDetails(final String link, final AbstractBuild<?, ?> owner,
            final AnnotationContainer container, final Collection<FileAnnotation> fixedAnnotations,
            final Collection<FileAnnotation> newAnnotations, final Collection<String> errors,
            final String defaultEncoding, final String displayName) {
        AnnotationContainer detail;
        if ("fixed".equals(link)) {
            detail = createFixedWarningsDetail(owner, fixedAnnotations, defaultEncoding, displayName);
        }
        else if ("new".equals(link)) {
            detail = new NewWarningsDetail(owner, this, newAnnotations, defaultEncoding, displayName);
        }
        else if ("error".equals(link)) {
            return new ErrorDetail(owner, errors);
        }
        else if (link.startsWith("tab.new")) {
            detail = createTabDetail(owner, newAnnotations, createGenericTabUrl(link), defaultEncoding);
        }
        else if (link.startsWith("tab.fixed")) {
            detail = createTabDetail(owner, fixedAnnotations, createGenericTabUrl(link), defaultEncoding);
        }
        else {
            return createDetails(link, owner, container, defaultEncoding, displayName);
        }
        attachLabelProvider(detail);
        return detail;
    }

    /**
     * @deprecated use {@link #createDetails(String, Run, AnnotationContainer, String, String)} instead
     */
    @Deprecated
    public Object createDetails(final String link, final AbstractBuild<?, ?> owner, final AnnotationContainer container,
            final String defaultEncoding, final String displayName) {
        PriorityDetailFactory factory = new PriorityDetailFactory(this);
        AnnotationContainer detail = null;
        if (factory.isPriority(link)) {
            detail = factory.create(link, owner, container, defaultEncoding, displayName);
        }
        else if (link.startsWith("module.")) {
            detail = new ModuleDetail(owner, this, container.getModule(createHashCode(link, "module.")), defaultEncoding, displayName);
        }
        else if (link.startsWith("package.")) {
            detail = new PackageDetail(owner, this, container.getPackage(createHashCode(link, "package.")), defaultEncoding, displayName);
        }
        else if (link.startsWith("file.")) {
            detail = new FileDetail(owner, this, container.getFile(createHashCode(link, "file.")), defaultEncoding, displayName);
        }
        else if (link.startsWith("tab.")) {
            detail = createTabDetail(owner, container.getAnnotations(), createGenericTabUrl(link), defaultEncoding);
        }
        else if (link.startsWith("source.")) {
            owner.checkPermission(Item.WORKSPACE);

            FileAnnotation annotation = container.getAnnotation(StringUtils.substringAfter(link, "source."));
            if (annotation.isInConsoleLog()) {
                LineRange lines = annotation.getLineRanges().iterator().next();
                return new ConsoleDetail(owner, lines.getStart(), lines.getEnd());
            }
            else {
                return new SourceDetail(owner, annotation, defaultEncoding);
            }
        }
        else if (link.startsWith("category.")) {
            DefaultAnnotationContainer category = container.getCategory(createHashCode(link, "category."));
            detail = createAttributeDetail(owner, category, displayName, Messages.CategoryDetail_header(), defaultEncoding);
        }
        else if (link.startsWith("type.")) {
            DefaultAnnotationContainer type = container.getType(createHashCode(link, "type."));
            detail = createAttributeDetail(owner, type, displayName, Messages.TypeDetail_header(), defaultEncoding);
        }
        if (detail != null) {
            attachLabelProvider(detail);
        }
        return detail;
    }
}
