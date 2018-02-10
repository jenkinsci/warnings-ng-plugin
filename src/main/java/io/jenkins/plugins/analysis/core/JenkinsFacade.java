package io.jenkins.plugins.analysis.core;

import java.io.Serializable;
import java.util.List;

import jenkins.model.Jenkins;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
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
     *
     * @param descriptorType
     *         the base type that represents the descriptor of the describable
     * @return the discovered instances, might be an empty list
     */
    public <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> getDescriptorsFor(Class<T> descriptorType) {
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
}
