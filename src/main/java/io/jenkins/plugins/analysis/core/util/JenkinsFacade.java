package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.acegisecurity.AccessDeniedException;
import org.apache.commons.lang3.StringUtils;

import com.google.errorprone.annotations.MustBeClosed;

import org.kohsuke.stapler.Stapler;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractItem;
import hudson.model.BallColor;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.security.Permission;
import jenkins.model.Jenkins;

/**
 * Facade to Jenkins server. Encapsulates all calls to the running Jenkins server so that tests can replace this facade
 * with a stub.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class JenkinsFacade implements Serializable {
    private static final long serialVersionUID = 1904631270145841113L;

    /**
     * Returns the lines of the console log. If the log cannot be read, then the exception message is returned as text.
     *
     * @param build
     *         the build to get the console log for
     *
     * @return the lines of the console log
     */
    @MustBeClosed
    public Stream<String> readConsoleLog(final Run<?, ?> build) {
        return new ConsoleLogReaderFactory(build).readStream();
    }

    /**
     * Returns the affected file with the specified file name.
     *
     * @param build
     *         the build to get the console log for
     * @param fileName
     *         the file to read
     * @param sourceEncoding
     *         the encoding to use when reading the file
     *
     * @return the affected file
     * @throws IOException if the file could not be read
     */
    @MustBeClosed
    public Reader readBuildFile(final Run<?, ?> build, final String fileName, final Charset sourceEncoding)
            throws IOException {
        InputStream inputStream = AffectedFilesResolver.asStream(build, fileName);

        return new InputStreamReader(inputStream, sourceEncoding);
    }

    /**
     * Returns the discovered instances for the given extension type.
     *
     * @param extensionType
     *         The base type that represents the extension point. Normally {@link ExtensionPoint} subtype but that's not
     *         a hard requirement.
     * @param <T>
     *         type of the extension
     *
     * @return the discovered instances, might be an empty list
     */
    public <T> List<T> getExtensionsFor(final Class<T> extensionType) {
        return getJenkins().getExtensionList(extensionType);
    }

    /**
     * Returns the discovered instances for the given descriptor type.
     *
     * @param <T>
     *         type of the describable
     * @param <D>
     *         type of the descriptor
     * @param describableType
     *         the base type that represents the descriptor of the describable
     *
     * @return the discovered instances, might be an empty list
     */
    public <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> getDescriptorsFor(
            final Class<T> describableType) {
        return getJenkins().getDescriptorList(describableType);
    }

    /**
     * Checks if the current security principal has this permission.
     *
     * @param permission
     *         the permission to check for
     *
     * @return {@code false} if the user doesn't have the permission
     */
    public boolean hasPermission(final Permission permission) {
        return getJenkins().getACL().hasPermission(permission);
    }

    /**
     * Gets a {@link Job} by its full name. Full names are like path names, where each name of {@link Item} is combined
     * by '/'.
     *
     * @param name
     *         the full name of the job
     *
     * @return the selected job, if it exists under the given full name and if it is accessible
     */
    @SuppressWarnings("unchecked")
    public Optional<Job<?, ?>> getJob(final String name) {
        try {
            return Optional.ofNullable(getJenkins().getItemByFullName(name, Job.class));
        }
        catch (AccessDeniedException ignore) {
            return Optional.empty();
        }
    }

    /**
     * Gets a {@link Run build} by the full ID.
     *
     * @param id
     *         the ID of the build
     *
     * @return the selected build, if it exists with the given ID and if it is accessible
     */
    @SuppressWarnings("unchecked")
    public Optional<Run<?, ?>> getBuild(final String id) {
        try {
            return Optional.ofNullable(Run.fromExternalizableId(id));
        }
        catch (AccessDeniedException ignore) {
            // ignore
        }
        return Optional.empty();
    }

    /**
     * Returns the absolute URL for the specified ball icon.
     *
     * @param color
     *         the color
     *
     * @return the absolute URL
     */
    public String getImagePath(final BallColor color) {
        return color.getImageOf("16x16");
    }

    /**
     * Returns the absolute URL for the specified icon.
     *
     * @param icon
     *         the icon URL
     *
     * @return the absolute URL
     */
    public String getImagePath(final String icon) {
        return Stapler.getCurrentRequest().getContextPath() + Jenkins.RESOURCE_PATH + icon;
    }

    /**
     * Returns an absolute URL for the specified url elements: e.g., creates the sequence ${rootUrl}/element1/element2.
     *
     * @param urlElements
     *         the url elements
     *
     * @return the absolute URL
     */
    public String getAbsoluteUrl(final String... urlElements) {
        return getAbsoluteUrl(StringUtils.join(urlElements, "/"));

    }

    private String getAbsoluteUrl(final String url) {
        try {
            String rootUrl = getJenkins().getRootUrl();
            if (rootUrl != null) {
                return rootUrl + "/" + url;
            }
        }
        catch (IllegalStateException ignored) {
            // ignored
        }
        return url;
    }

    /**
     * Returns the full names of all available jobs. The full name is given by {@link AbstractItem#getFullName()}.
     *
     * @return the full names of all jobs
     */
    public Set<String> getAllJobs() {
        return getJenkins().getAllItems(Job.class).stream()
                .map(this::getFullNameOf).collect(Collectors.toSet());
    }

    /**
     * Returns the full name of the specified job.
     *
     * @param job
     *         the job to get the name for
     *
     * @return the full name
     */
    public String getFullNameOf(final Job<?, ?> job) {
        return job.getFullName(); // getFullName is final
    }

    private Jenkins getJenkins() {
        return Jenkins.getInstance();
    }
}
