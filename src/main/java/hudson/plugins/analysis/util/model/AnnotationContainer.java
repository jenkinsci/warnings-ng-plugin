package hudson.plugins.analysis.util.model; // NOPMD

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import hudson.plugins.analysis.Messages;

/**
 * A container for annotations.
 *
 * @author Ulli Hafner
 */
public abstract class AnnotationContainer implements AnnotationProvider, Serializable, Comparable<AnnotationContainer> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 855696821788264261L;
    /** The hierarchy of a container. */
    public enum Hierarchy {
        /** Project level. */
        PROJECT,
        /** Module level. */
        MODULE,
        /** Package level. */
        PACKAGE,
        /** File level. */
        FILE}

    /** The annotations mapped by their key. */
    @SuppressWarnings("Se")
    private final Map<Long, FileAnnotation> annotations = new HashMap<Long, FileAnnotation>();
    /** The annotations mapped by priority. */
    private transient Map<Priority, Set<FileAnnotation>> annotationsByPriority;
    /** The annotations mapped by category. */
    private transient Map<String, Set<FileAnnotation>> annotationsByCategory;
    /** The annotations mapped by type. */
    private transient Map<String, Set<FileAnnotation>> annotationsByType;
    /** The files that contain annotations mapped by file name. */
    private transient Map<String, WorkspaceFile> filesByName;
    /** The packages that contain annotations mapped by package name. */
    private transient Map<String, JavaPackage> packagesByName;
    /** The modules that contain annotations mapped by module name. */
    private transient Map<String, MavenModule> modulesByName;
    /** The files that contain annotations mapped by hash code of file name. */
    private transient Map<Integer, WorkspaceFile> filesByHashCode;
    /** The packages that contain annotations mapped by hash code of package name. */
    private transient Map<Integer, JavaPackage> packagesByHashCode;
    /** The modules that contain annotations mapped by hash code of module name. */
    private transient Map<Integer, MavenModule> modulesByHashCode;
    /** The modules that contain annotations mapped by hash code of category name. */
    private transient Map<Integer, Set<FileAnnotation>> categoriesByHashCode;
    /** The modules that contain annotations mapped by hash code of type name. */
    private transient Map<Integer, Set<FileAnnotation>> typesByHashCode;

    /** Determines whether to build up a set of {@link WorkspaceFile}s. */
    @java.lang.SuppressWarnings("unused")
    private boolean handleFiles; // backward compatibility NOPMD

    /** Name of this container. */
    private String name;
    /** Hierarchy level of this container. */
    private Hierarchy hierarchy;

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     *
     * @param hierarchy the hierarchy of this container
     */
    public AnnotationContainer(final Hierarchy hierarchy) {
        this(StringUtils.EMPTY, hierarchy);
    }

    /**
     * Returns this container.
     *
     * @return this container
     */
    public AnnotationContainer getContainer() {
        return this;
    }

    /**
     * Gets the maximum number of annotations within the specified containers.
     *
     * @param containers
     *            the containers to scan for the upper bound
     * @return the maximum number of annotations
     */
    public int getUpperBound(final Collection<? extends AnnotationContainer> containers) {
        int maximum = 0;
        for (AnnotationContainer container : containers) {
            maximum = Math.max(maximum, container.getNumberOfAnnotations());
        }
        return maximum;
    }

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     *
     * @param name the name of this container
     * @param hierarchy the hierarchy of this container
     */
    protected AnnotationContainer(final String name, final Hierarchy hierarchy) {
        initialize();
        this.name = name;
        this.hierarchy = hierarchy;
    }

    /**
     * Sets the hierarchy to the specified value.
     *
     * @param hierarchy the value to set
     */
    protected void setHierarchy(final Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * Returns the name of this container.
     *
     * @return the name of this container
     */
    public final String getName() {
        return name;
    }

    /**
     * Sets the name of this container.
     *
     * @param name the name of this container
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * Initializes the transient mappings.
     */
    private void initialize() {
        annotationsByPriority = new EnumMap<Priority, Set<FileAnnotation>>(Priority.class);
        for (Priority priority : Priority.values()) {
            annotationsByPriority.put(priority, new HashSet<FileAnnotation>());
        }
        annotationsByCategory = new HashMap<String, Set<FileAnnotation>>();
        annotationsByType = new HashMap<String, Set<FileAnnotation>>();
        filesByName = new HashMap<String, WorkspaceFile>();
        packagesByName = new HashMap<String, JavaPackage>();
        modulesByName = new HashMap<String, MavenModule>();
        filesByHashCode = new HashMap<Integer, WorkspaceFile>();
        packagesByHashCode = new HashMap<Integer, JavaPackage>();
        modulesByHashCode = new HashMap<Integer, MavenModule>();
        categoriesByHashCode = new HashMap<Integer, Set<FileAnnotation>>();
        typesByHashCode = new HashMap<Integer, Set<FileAnnotation>>();
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    @SuppressWarnings("Se")
    private Object readResolve() {
        rebuildMappings();
        return this;
    }

    /**
     * Rebuilds the priorities and files after deserialization.
     */
    protected void rebuildMappings() {
        initialize();
        for (FileAnnotation annotation : getAnnotations()) {
            updateMappings(annotation);
        }
    }

    /**
     * Updates the annotation drill-down mappings (priority, packages, files) with the specified annotation.
     *
     * @param annotation the new annotation
     */
    private void updateMappings(final FileAnnotation annotation) {
        annotationsByPriority.get(annotation.getPriority()).add(annotation);
        if (StringUtils.isNotBlank(annotation.getCategory())) {
            addCategory(annotation);
        }
        if (StringUtils.isNotBlank(annotation.getType())) {
            addType(annotation);
        }
        if (hierarchy == Hierarchy.PROJECT) {
            addModule(annotation);
        }
        if (hierarchy == Hierarchy.PROJECT || hierarchy == Hierarchy.MODULE) {
            addPackage(annotation);
        }
        if (hierarchy == Hierarchy.PROJECT || hierarchy == Hierarchy.MODULE || hierarchy == Hierarchy.PACKAGE) {
            addFile(annotation);
        }
    }

    /**
     * Adds a new category to this container that will contain the specified
     * annotation. If the category already exists, then the annotation is only added
     * to this category.
     *
     * @param annotation the new annotation
     */
    private void addCategory(final FileAnnotation annotation) {
        String category = annotation.getCategory();
        if (!annotationsByCategory.containsKey(category)) {
            HashSet<FileAnnotation> container = new HashSet<FileAnnotation>();
            annotationsByCategory.put(category, container);
            categoriesByHashCode.put(category.hashCode(), container);
        }
        annotationsByCategory.get(category).add(annotation);
    }

    /**
     * Adds a new type to this container that will contain the specified
     * annotation. If the type already exists, then the annotation is only added
     * to this type.
     *
     * @param annotation the new annotation
     */
    private void addType(final FileAnnotation annotation) {
        String type = annotation.getType();
        if (!annotationsByType.containsKey(type)) {
            HashSet<FileAnnotation> container = new HashSet<FileAnnotation>();
            annotationsByType.put(type, container);
            typesByHashCode.put(type.hashCode(), container);
        }
        annotationsByType.get(type).add(annotation);
    }

    /**
     * Adds a new module to this container that will contain the specified
     * annotation. If the module already exists, then the annotation is only added
     * to this module.
     *
     * @param annotation the new annotation
     */
    private void addModule(final FileAnnotation annotation) {
        String moduleName = annotation.getModuleName();
        if (!modulesByName.containsKey(moduleName)) {
            MavenModule module = new MavenModule(moduleName);
            modulesByName.put(moduleName, module);
            modulesByHashCode.put(moduleName.hashCode(), module);
        }
        modulesByName.get(moduleName).addAnnotation(annotation);
    }

    /**
     * Adds a new package to this container that will contain the specified
     * annotation. If the package already exists, then the annotation is only added
     * to this package.
     *
     * @param annotation the new annotation
     */
    private void addPackage(final FileAnnotation annotation) {
        String packageName = annotation.getPackageName();
        if (!packagesByName.containsKey(packageName)) {
            JavaPackage javaPackage = new JavaPackage(packageName);
            packagesByName.put(packageName, javaPackage);
            packagesByHashCode.put(packageName.hashCode(), javaPackage);
        }
        packagesByName.get(packageName).addAnnotation(annotation);
    }

    /**
     * Adds a new file to this container that will contain the specified
     * annotation. If the file already exists, then the annotation is only added
     * to this class.
     *
     * @param annotation the new annotation
     */
    private void addFile(final FileAnnotation annotation) {
        String fileName = annotation.getFileName();
        if (!filesByName.containsKey(fileName)) {
            WorkspaceFile file = new WorkspaceFile(fileName);
            filesByName.put(fileName, file);
            filesByHashCode.put(file.getName().hashCode(), file);
        }
        filesByName.get(fileName).addAnnotation(annotation);
    }

    /**
     * Adds the specified annotation to this container.
     *
     * @param annotation the annotation to add
     */
    public final void addAnnotation(final FileAnnotation annotation) {
        annotations.put(annotation.getKey(), annotation);
        updateMappings(annotation);
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations the annotations to add
     */
    public final void addAnnotations(final Collection<? extends FileAnnotation> newAnnotations) {
        for (FileAnnotation annotation : newAnnotations) {
            addAnnotation(annotation);
        }
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations the annotations to add
     */
    public final void addAnnotations(final FileAnnotation[] newAnnotations) {
        addAnnotations(Arrays.asList(newAnnotations));
    }

    /**
     * Returns a sorted set of the annotations.
     *
     * @return a sorted set  of the annotations
     */
    public final SortedSet<FileAnnotation> getSortedAnnotations() {
        return ImmutableSortedSet.copyOf(annotations.values());
    }

    /** {@inheritDoc} */
    public final Set<FileAnnotation> getAnnotations() {
        return ImmutableSet.copyOf(annotations.values());
    }

    /** {@inheritDoc} */
    public final Set<FileAnnotation> getAnnotations(final Priority priority) {
        return ImmutableSortedSet.copyOf(annotationsByPriority.get(priority));
    }

    /**
     * Returns the annotations with {@link Priority#HIGH}.
     *
     * @return the annotations with {@link Priority#HIGH}
     */
    public final Set<FileAnnotation> getHighAnnotations() {
        return getAnnotations(Priority.HIGH);
    }

    /**
     * Returns the annotations with {@link Priority#NORMAL}.
     *
     * @return the annotations with {@link Priority#NORMAL}
     */
    public final Set<FileAnnotation> getNormalAnnotations() {
        return getAnnotations(Priority.NORMAL);
    }

    /**
     * Returns the annotations with {@link Priority#LOW}.
     *
     * @return the annotations with {@link Priority#LOW}
     */
    public final Set<FileAnnotation> getLowAnnotations() {
        return getAnnotations(Priority.LOW);
    }

    /** {@inheritDoc} */
    public final Set<FileAnnotation> getAnnotations(final String priority) {
        return getAnnotations(getPriority(priority));
    }

    /**
     * Converts a String priority to an actual enumeration value.
     *
     * @param priority priority as a String
     *
     * @return enumeration value.
     */
    private Priority getPriority(final String priority) {
        return Priority.fromString(priority);
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations() {
        return annotations.size();
    }

    /**
     * Gets the number of annotations with priority low.
     *
     * @return the number of annotations with priority low
     */
    public int getNumberOfLowAnnotations() {
        return getLowAnnotations().size();
    }

    /**
     * Gets the number of annotations with priority normal.
     *
     * @return the number of annotations with priority normal
     */
    public int getNumberOfNormalAnnotations() {
        return getNormalAnnotations().size();
    }

    /**
     * Gets the number of annotations with priority high.
     *
     * @return the number of annotations with priority high
     */
    public int getNumberOfHighAnnotations() {
        return getHighAnnotations().size();
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations(final Priority priority) {
        return annotationsByPriority.get(priority).size();
    }

    /** {@inheritDoc} */
    public final int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations() {
        return !hasNoAnnotations();
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final Priority priority) {
        return !hasNoAnnotations(priority);
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final String priority) {
        return !hasNoAnnotations(priority);
    }

    /** {@inheritDoc} */
    public final boolean hasNoAnnotations() {
        return annotations.isEmpty();
    }

    /** {@inheritDoc} */
    public final boolean hasNoAnnotations(final Priority priority) {
        return annotationsByPriority.get(priority).isEmpty();
    }

    /** {@inheritDoc} */
    public final boolean hasNoAnnotations(final String priority) {
        return hasNoAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final long key) {
        FileAnnotation annotation = annotations.get(key);
        if (annotation != null) {
            return annotation;
        }
        throw new NoSuchElementException("Annotation not found: key=" + key);
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final String key) {
        return getAnnotation(Long.parseLong(key));
    }

    /**
     * Returns a tooltip showing the distribution of priorities for this container.
     *
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip() {
        StringBuilder message = new StringBuilder();
        String separator = " - ";
        for (Priority priority : Priority.values()) {
            if (hasAnnotations(priority)) {
                message.append(priority.getLocalizedString());
                message.append(":");
                message.append(getNumberOfAnnotations(priority));
                message.append(separator);
            }
        }
        return StringUtils.removeEnd(message.toString(), separator);
    }


    /**
     * Returns the package category name for the scanned files. Currently, only
     * java and c# files are supported.
     *
     * @return the package category name for the scanned files
     */
    public final String getPackageCategoryName() {
        if (hasAnnotations()) {
            String fileName = getAnnotations().iterator().next().getFileName();
            if (fileName.endsWith(".cs")) {
                return Messages.NamespaceDetail_header();
            }
        }
        return Messages.PackageDetail_header();
    }

    /**
     * Gets the modules of this container that have annotations.
     *
     * @return the modules with annotations
     */
    public Collection<MavenModule> getModules() {
        ArrayList<MavenModule> modules = new ArrayList<MavenModule>(modulesByName.values());
        Collections.sort(modules);
        return Collections.unmodifiableCollection(modules);
    }

    /**
     * Returns whether the maven module with the given name exists.
     *
     * @param moduleName the module to check for
     *
     * @return <code>true</code> if the maven module with the given name
     * exists, <code>false</code> otherwise
     */
    public boolean containsModule(final String moduleName) {
        return modulesByName.containsKey(moduleName);
    }

    /**
     * Gets the module with the given name.
     *
     * @param moduleName the name of the module
     *
     * @return the module with the given name
     */
    public MavenModule getModule(final String moduleName) {
        if (modulesByName.containsKey(moduleName)) {
            return modulesByName.get(moduleName);
        }
        throw new NoSuchElementException("Module not found: " + moduleName);
    }

    /**
     * Gets the module with the given hash code.
     *
     * @param hashCode the hash code of the module
     *
     * @return the module with the given name
     */
    public MavenModule getModule(final int hashCode) {
        if (modulesByHashCode.containsKey(hashCode)) {
            return modulesByHashCode.get(hashCode);
        }
        throw new NoSuchElementException("Module by hashcode not found: " + hashCode);
    }

    /**
     * Gets the packages of this container that have annotations.
     *
     * @return the packages with annotations
     */
    public Collection<JavaPackage> getPackages() {
        ArrayList<JavaPackage> packages = new ArrayList<JavaPackage>(packagesByName.values());
        Collections.sort(packages);
        return Collections.unmodifiableCollection(packages);
    }

    /**
     * Returns whether the package with the given name exists.
     *
     * @param packageName the package to check for
     *
     * @return <code>true</code> if the package with the given name
     * exists, <code>false</code> otherwise
     */
    public boolean containsPackage(final String packageName) {
        return packagesByName.containsKey(packageName);
    }

    /**
     * Gets the package with the given name.
     *
     * @param packageName the name of the package
     *
     * @return the file with the given name
     */
    public JavaPackage getPackage(final String packageName) {
        if (packagesByName.containsKey(packageName)) {
            return packagesByName.get(packageName);
        }
        throw new NoSuchElementException("Package not found: " + packageName);
    }

    /**
     * Gets the package with the given hash code.
     *
     * @param hashCode the hash code of the package
     *
     * @return the package with the given name
     */
    public JavaPackage getPackage(final int hashCode) {
        if (packagesByHashCode.containsKey(hashCode)) {
            return packagesByHashCode.get(hashCode);
        }
        throw new NoSuchElementException("Package by hashcode not found: " + hashCode);
    }

    /**
     * Gets the files of this container that have annotations.
     *
     * @return the files with annotations
     */
    public Collection<WorkspaceFile> getFiles() {
        ArrayList<WorkspaceFile> files = new ArrayList<WorkspaceFile>(filesByName.values());
        Collections.sort(files);
        return Collections.unmodifiableCollection(files);
    }

    /**
     * Returns whether the file with the given name exists.
     *
     * @param fileName the file to check for
     *
     * @return <code>true</code> if the file with the given name
     * exists, <code>false</code> otherwise
     */
    public boolean containsFile(final String fileName) {
        return filesByName.containsKey(fileName);
    }

    /**
     * Gets the file with the given name.
     *
     * @param fileName the short name of the file
     *
     * @return the file with the given name
     */
    public WorkspaceFile getFile(final String fileName) {
        if (filesByName.containsKey(fileName)) {
            return filesByName.get(fileName);
        }
        throw new NoSuchElementException("File not found: " + fileName);
    }

    /**
     * Gets the file with the given hash code.
     *
     * @param hashCode the hash code of the file
     *
     * @return the file with the given name
     */
    public WorkspaceFile getFile(final int hashCode) {
        if (filesByHashCode.containsKey(hashCode)) {
            return filesByHashCode.get(hashCode);
        }
        throw new NoSuchElementException("File by hashcode not found: " + hashCode);
    }

    /**
     * Gets the categories of this container that have annotations.
     *
     * @return the categories with annotations
     */
    public Collection<AnnotationContainer> getCategories() {
        ArrayList<AnnotationContainer> categories = new ArrayList<AnnotationContainer>();
        for (String category : annotationsByCategory.keySet()) {
            categories.add(getCategory(category));
        }
        Collections.sort(categories);
        return categories;
    }

    /**
     * Returns whether the category with the given name exists.
     *
     * @param category the file to check for
     *
     * @return <code>true</code> if the category with the given name
     * exists, <code>false</code> otherwise
     */
    public boolean containsCategory(final String category) {
        return annotationsByCategory.containsKey(category);
    }

    /**
     * Gets the category with the given name.
     *
     * @param category the category name
     * @return the category with the given name
     */
    public DefaultAnnotationContainer getCategory(final String category) {
        if (annotationsByCategory.containsKey(category)) {
            return new DefaultAnnotationContainer(category, annotationsByCategory.get(category));
        }
        throw new NoSuchElementException("Category not found: " + category);
    }

    /**
     * Gets the category with the given hash code.
     *
     * @param hashCode the category hash code
     * @return the category with the given hash code
     */
    public DefaultAnnotationContainer getCategory(final int hashCode) {
        if (categoriesByHashCode.containsKey(hashCode)) {
            Set<FileAnnotation> container = categoriesByHashCode.get(hashCode);
            FileAnnotation fileAnnotation = container.iterator().next();
            return new DefaultAnnotationContainer(fileAnnotation.getCategory(), container);
        }
        throw new NoSuchElementException("Category by hashCode not found: " + hashCode);
    }

    /**
     * Gets the types of this container that have annotations.
     *
     * @return the types with annotations
     */
    public Collection<AnnotationContainer> getTypes() {
        ArrayList<AnnotationContainer> types = new ArrayList<AnnotationContainer>();
        for (String type : annotationsByType.keySet()) {
            types.add(getType(type));
        }
        Collections.sort(types);
        return types;
    }

    /**
     * Returns whether the type with the given name exists.
     *
     * @param type the type to check for
     *
     * @return <code>true</code> if the type with the given name
     * exists, <code>false</code> otherwise
     */
    public boolean containsType(final String type) {
        return annotationsByType.containsKey(type);
    }

    /**
     * Gets the type with the given name.
     *
     * @param type the type name
     * @return the type with the given name
     */
    public DefaultAnnotationContainer getType(final String type) {
        if (annotationsByType.containsKey(type)) {
            return new DefaultAnnotationContainer(type, annotationsByType.get(type));
        }
        throw new NoSuchElementException("Type not found: " + type);
    }

    /**
     * Gets the type with the given hash code.
     *
     * @param hashCode the type hash code
     * @return the type with the given hash code
     */
    public DefaultAnnotationContainer getType(final int hashCode) {
        if (typesByHashCode.containsKey(hashCode)) {
            Set<FileAnnotation> container = typesByHashCode.get(hashCode);
            FileAnnotation fileAnnotation = container.iterator().next();
            return new DefaultAnnotationContainer(fileAnnotation.getType(), container);
        }
        throw new NoSuchElementException("Type by hashcode not found: " + hashCode);
    }

    /**
     * Returns {@link Priority#HIGH}.
     *
     * @return {@link Priority#HIGH}
     */
    public Priority getHighPriority() {
        return Priority.HIGH;
    }

    /**
     * Returns {@link Priority#NORMAL}.
     *
     * @return {@link Priority#NORMAL}
     */
    public Priority getNormalPriority() {
        return Priority.NORMAL;
    }

    /**
     * Returns {@link Priority#LOW}.
     *
     * @return {@link Priority#LOW}
     */
    public Priority getLowPriority() {
        return Priority.LOW;
    }

    /** {@inheritDoc} */
    public int compareTo(final AnnotationContainer other) {
        return getName().compareTo(other.getName());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AnnotationContainer other = (AnnotationContainer)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
