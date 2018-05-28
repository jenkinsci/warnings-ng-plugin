package io.jenkins.plugins.analysis.core;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.acegisecurity.AccessDeniedException;
import org.apache.commons.lang3.StringUtils;

import jenkins.model.Jenkins;

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

/**
 * Facade to Jenkins server. Encapsulates all calls to the running Jenkins server so that tests can replace this facade
 * with a stub.
 *
 * @author Ullrich Hafner
 */
public class JenkinsFacade implements Serializable {
    private static final long serialVersionUID = 1904631270145841113L;

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
     * @param descriptorType
     *         the base type that represents the descriptor of the describable
     *
     * @return the discovered instances, might be an empty list
     */
    public <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> getDescriptorsFor(
            final Class<T> descriptorType) {
        return getJenkins().getDescriptorList(descriptorType);
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

    private Jenkins getJenkins() {
        return Jenkins.getInstance();
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
     * Gets a {@link Run} by the full name of its job and by the ID within this job. Full names are like path names,
     * where each name of {@link Item} is combined by '/'.
     *
     * @param name
     *         the full name of the job
     * @param id
     *         the ID of the build
     *
     * @return the selected build, if it exists under the given full name and ID and if it is accessible
     */
    @SuppressWarnings("unchecked")
    public Optional<Run<?, ?>> getBuild(final String name, final String id) {
        try {
            Optional<Job<?, ?>> job = getJob(name);
            if (job.isPresent()) {
                return Optional.ofNullable(job.get().getBuild(id));
            }
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
                .map(this::getFullNameOf).distinct().collect(Collectors.toSet());
    }

    /**
     * Returns the full name of the specified job.
     *
     * @param job
     *         the job to get the name for
     *
     * @return the full name
     */
    public String getFullNameOf(final Job job) {
        return job.getFullName(); // getFullName is final
    }
}
