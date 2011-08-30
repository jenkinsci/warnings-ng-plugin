package hudson.plugins.analysis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.digester.Digester;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Detects module names by parsing the name of a source file, the Maven pom.xml
 * file or the ANT build.xml file.
 *
 * @author Ulli Hafner
 * @author Christoph Laeubrich (support for OSGi-Bundles)
 */
public class ModuleDetector {
    private static final String PLUS = ", ";
    private static final String BACK_SLASH = "\\";
    private static final String SLASH = "/";
    private static final String ALL_DIRECTORIES = "**/";

    private static final String BUNDLE_VENDOR = "Bundle-Vendor";
    private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    private static final String BUNDLE_NAME = "Bundle-Name";
    private static final String REPLACEMENT_CHAR = "%";

    static final String MAVEN_POM = "pom.xml";
    static final String ANT_PROJECT = "build.xml";
    static final String OSGI_BUNDLE = "META-INF/MANIFEST.MF";

    private static final String PATTERN = ALL_DIRECTORIES + MAVEN_POM
            + PLUS + ALL_DIRECTORIES + ANT_PROJECT
            + PLUS + ALL_DIRECTORIES + OSGI_BUNDLE;

    /** The factory to create input streams with. */
    private FileInputStreamFactory factory = new DefaultFileInputStreamFactory();
    /** Maps file names to module names. */
    private final Map<String, String> fileNameToModuleName;
    /** Sorted list of file name prefixes. */
    private final List<String> prefixes;

    /**
     * Creates a new instance of {@link ModuleDetector}.
     */
    protected ModuleDetector() {
        fileNameToModuleName = new HashMap<String, String>();
        prefixes = new ArrayList<String>();
    }

    /**
     * Creates a new instance of {@link ModuleDetector}.
     *
     * @param workspace
     *            the workspace to scan for Maven pom.xml or Ant build.xml files
     */
    public ModuleDetector(final File workspace) {
        this(workspace, new DefaultFileInputStreamFactory());
    }

    /**
     * Creates a new instance of {@link ModuleDetector}.
     *
     * @param workspace
     *            the workspace to scan for Maven pom.xml or ant build.xml files
     * @param fileInputStreamFactory
     *            factory to load files
     */
    ModuleDetector(final File workspace, final FileInputStreamFactory fileInputStreamFactory) {
        factory = fileInputStreamFactory;
        fileNameToModuleName = createFilesToModuleMapping(workspace);
        prefixes = new ArrayList<String>(fileNameToModuleName.keySet());
        Collections.sort(prefixes);
    }

    /**
     * Returns a mapping of path prefixes to module names.
     *
     * @param workspace
     *            the workspace to start scanning for files
     * @return the mapping of path prefixes to module names
     */
    private Map<String, String> createFilesToModuleMapping(final File workspace) {
        Map<String, String> mapping = new HashMap<String, String>();

        String[] projects = find(workspace);
        for (String fileName : projects) {
            if (fileName.endsWith(ANT_PROJECT)) {
                addMapping(mapping, fileName, ANT_PROJECT, parseBuildXml(fileName));
            }
        }
        for (String fileName : projects) {
            if (fileName.endsWith(MAVEN_POM)) {
                addMapping(mapping, fileName, MAVEN_POM, parsePom(fileName));
            }
        }
        for (String fileName : projects) {
            if (fileName.endsWith(OSGI_BUNDLE)) {
                addMapping(mapping, fileName, OSGI_BUNDLE, parseManifest(fileName));
            }
        }

        return mapping;
    }

    private void addMapping(final Map<String, String> mapping, final String fileName, final String suffix, final String moduleName) {
        if (StringUtils.isNotBlank(moduleName)) {
            mapping.put(StringUtils.substringBeforeLast(fileName, suffix), moduleName);
        }
    }

    /**
     * Uses the path prefixes of pom.xml or build.xml files to guess a module
     * name for the specified file.
     *
     * @param originalFileName
     *            file name to guess a module for, must be an absolute path
     * @return a module name or an empty string
     */
    public String guessModuleName(final String originalFileName) {
        String fullPath = originalFileName.replace('\\', '/');

        String guessedModule = StringUtils.EMPTY;
        for (String path : prefixes) {
            if (fullPath.startsWith(path)) {
                guessedModule = fileNameToModuleName.get(path);
            }
        }
        return guessedModule;
    }

    /**
     * Finds files of the matching pattern.
     *
     * @param path
     *            root path to scan in
     * @return the found files (as absolute paths)
     */
    private String[] find(final File path) {
        String[] relativeFileNames = factory.find(path, PATTERN);
        String[] absoluteFileNames = new String[relativeFileNames.length];

        String absolutePath = path.getAbsolutePath();
        for (int file = 0; file < absoluteFileNames.length; file++) {
            if (!relativeFileNames[file].startsWith(SLASH)) {
                String absoluteName = absolutePath + SLASH + relativeFileNames[file];
                absoluteFileNames[file] = absoluteName.replace(BACK_SLASH, SLASH);
            }
            else {
                absoluteFileNames[file] = relativeFileNames[file];
            }
        }
        return absoluteFileNames;
    }

    /**
     * Returns the project name stored in the build.xml.
     *
     * @param buildXml
     *            Ant build.xml file name
     * @return the project name or an empty string if the name could not be
     *         resolved
     */
    private String parseBuildXml(final String buildXml) {
        InputStream file = null;
        try {
            file = factory.create(buildXml);
            Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(ModuleDetector.class.getClassLoader());

            digester.push(new StringBuffer());
            String xPath = "project";
            digester.addCallMethod(xPath, "append", 1);
            digester.addCallParam(xPath, 0, "name");

            StringBuffer result = (StringBuffer)digester.parse(file);
            return result.toString();
        }
        catch (IOException exception) {
            // ignore
        }
        catch (SAXException exception) {
            // ignore
        }
        finally {
            IOUtils.closeQuietly(file);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns the project name stored in the POM.
     *
     * @param pom
     *            Maven POM file name
     * @return the project name or an empty string if the name could not be
     *         resolved
     */
    private String parsePom(final String pom) {
        InputStream file = null;
        try {
            file = factory.create(pom);
            Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(ModuleDetector.class.getClassLoader());

            digester.push(new StringBuffer());
            digester.addCallMethod("project/name", "append", 0);

            StringBuffer result = (StringBuffer)digester.parse(file);
            return result.toString();
        }
        catch (IOException exception) {
            // ignore
        }
        catch (SAXException exception) {
            // ignore
        }
        finally {
            IOUtils.closeQuietly(file);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Scans a Manifest file for OSGi Bundle Information.
     *
     * @param manifestFile
     *            file name of MANIFEST.MF
     * @return the project name or an empty string if the name could not be
     *         resolved
     */
    private String parseManifest(final String manifestFile) {
        InputStream file = null;
        try {
            file = factory.create(manifestFile);
            Manifest manifest = new Manifest(file);
            Attributes attributes = manifest.getMainAttributes();
            Properties properties = readProperties(StringUtils.substringBefore(manifestFile, OSGI_BUNDLE));
            String name = getLocalizedValue(attributes, properties, BUNDLE_NAME);
            if (StringUtils.isNotBlank(name)) {
                return name;
            }
            return getSymbolicName(attributes, properties);
        }
        catch (IOException exception) {
            // ignore
        }
        finally {
            IOUtils.closeQuietly(file);
        }
        return StringUtils.EMPTY;
    }

    private String getLocalizedValue(final Attributes attributes, final Properties properties, final String bundleName) {
        String value = attributes.getValue(bundleName);
        if (StringUtils.startsWith(StringUtils.trim(value), REPLACEMENT_CHAR)) {
            return properties.getProperty(StringUtils.substringAfter(value, REPLACEMENT_CHAR));
        }
        return value;
    }

    private Properties readProperties(final String path) {
        Properties properties = new Properties();
        readProperties(path, properties, "plugin.properties");
        readProperties(path, properties, "OSGI-INF/l10n/bundle.properties");

        return properties;
    }

    private void readProperties(final String path, final Properties properties, final String fileName) {
        InputStream file = null;
        try {
            file = factory.create(path + SLASH + fileName);
            if (file != null) {
                properties.load(file);
            }
        }
        catch (IOException exception) {
            // ignore if properties are not present or not readable
        }
        finally {
            IOUtils.closeQuietly(file);
        }
    }

    private String getSymbolicName(final Attributes attributes, final Properties properties) {
        String symbolicName = StringUtils.substringBefore(attributes.getValue(BUNDLE_SYMBOLIC_NAME), ";");
        if (StringUtils.isNotBlank(symbolicName)) {
            String vendor = getLocalizedValue(attributes, properties, BUNDLE_VENDOR);
            if (StringUtils.isNotBlank(vendor)) {
                return symbolicName + " (" + vendor + ")";
            }
            else {
                return symbolicName;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * An input stream factory based on a {@link FileInputStream}.
     */
    private static final class DefaultFileInputStreamFactory implements FileInputStreamFactory {
        public InputStream create(final String fileName) throws FileNotFoundException {
            return new FileInputStream(new File(fileName));
        }

        public String[] find(final File root, final String pattern) {
            return new FileFinder(PATTERN).find(root);
        }
    }
}

