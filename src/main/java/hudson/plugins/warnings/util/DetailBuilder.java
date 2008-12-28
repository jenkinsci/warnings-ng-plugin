package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.AnnotationContainer;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * Creates detail objects for the selected element of a annotation container.
 *
 * @author Ulli Hafner
 */
public class DetailBuilder {
    /**
     * Returns a detail object for the selected element of the specified annotation container.
     * The details will include the new and fixed warnings trends as well as the errors report.
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
            return new ModuleDetail(owner, container.getModule(Integer.valueOf(StringUtils.substringAfter(link, "module."))), defaultEncoding, displayName);
        }
        else if (link.startsWith("package.")) {
            return new PackageDetail(owner, container.getPackage(Integer.valueOf(StringUtils.substringAfter(link, "package."))), defaultEncoding, displayName);
        }
        else if (link.startsWith("file.")) {
            return new FileDetail(owner, container.getFile(Integer.valueOf(StringUtils.substringAfter(link, "file."))), defaultEncoding, displayName);
        }
        else if (link.startsWith("tab.")) {
            return new TabDetail(owner, container.getAnnotations(), "/tabview/" + StringUtils.substringAfter(link, "tab.") + ".jelly", defaultEncoding);
        }
        else if (link.startsWith("source.")) {
            return new SourceDetail(owner, container.getAnnotation(StringUtils.substringAfter(link, "source.")), defaultEncoding);
        }
        else if (link.startsWith("category.")) {
            String category = StringUtils.substringAfter(link, "category.");
            return new AttributeDetail(owner, container.getCategory(category).getAnnotations(), defaultEncoding, displayName, Messages.CategoryDetail_header() + " " + category);
        }
        else if (link.startsWith("type.")) {
            String type = StringUtils.substringAfter(link, "type.");
            return new AttributeDetail(owner, container.getType(type).getAnnotations(), defaultEncoding, displayName, Messages.TypeDetail_header() + " " + type);
        }
        return null;
    }
}

