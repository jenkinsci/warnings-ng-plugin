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

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Detects module names by parsing the name of a source file, the Maven pom.xml file or the ANT build.xml file.
 *
 * @author Ulli Hafner
 */
public class ModuleDetector {
    private static final String BACK_SLASH = "\\";
    private static final String SLASH = "/";
    private static final String ALL_DIRECTORIES = "**/";

    static final String MAVEN_POM = "pom.xml";
    static final String ANT_PROJECT = "build.xml";
    private static final String PATTERN = ALL_DIRECTORIES + MAVEN_POM + ", " + ALL_DIRECTORIES + ANT_PROJECT;

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
     * @param fileInputStreamFactory the value to set
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ModuleDetector(final File workspace, final FileInputStreamFactory fileInputStreamFactory) {
        setFileInputStreamFactory(fileInputStreamFactory);
        fileNameToModuleName = createFilesToModuleMapping(workspace);
        prefixes = new ArrayList<String>(fileNameToModuleName.keySet());
        Collections.sort(prefixes);
    }

    /**
     * Sets the factory to the specified value.
     *
     * @param fileInputStreamFactory the value to set
     */
    public final void setFileInputStreamFactory(final FileInputStreamFactory fileInputStreamFactory) {
        factory = fileInputStreamFactory;
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

        String[] projects = factory.find(workspace, PATTERN);
        for (String fileName : projects) {
            if (fileName.endsWith(MAVEN_POM)) {
                addMapping(mapping, fileName, MAVEN_POM, parsePom(fileName));
            }
            else {
                addMapping(mapping, fileName, ANT_PROJECT, parseBuildXml(fileName));
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
     *            file name to guess a module for
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
     * @param pattern
     *            pattern of files
     * @return the found files
     */
    protected String[] find(final File path, final String pattern) {
        String[] relativeFileNames = factory.find(path, PATTERN);
        String[] absoluteFileNames = new String[relativeFileNames.length];

        String absolutePath = path.getAbsolutePath();
        for (int file = 0; file < absoluteFileNames.length; file++) {
            absoluteFileNames[file] = (absolutePath + SLASH + relativeFileNames[file]).replace(BACK_SLASH, SLASH);
        }
        return absoluteFileNames;
    }

    /**
     * Returns the project name stored in the build.xml.
     *
     * @param path
     *            root folder
     * @return the project name or an empty string if the name could not be
     *         resolved
     */
    private String parseBuildXml(final String path) {
        try {
            InputStream pom = factory.create(path);
            Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(ModuleDetector.class.getClassLoader());

            digester.push(new StringBuffer());
            String xPath = "project";
            digester.addCallMethod(xPath, "append", 1);
            digester.addCallParam(xPath, 0, "name");

            StringBuffer result = (StringBuffer)digester.parse(pom);
            return result.toString();
        }
        catch (IOException exception) {
            // ignore
        }
        catch (SAXException exception) {
            // ignore
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns the project name stored in the POM.
     *
     * @param fileName
     *            Maven module root folder
     * @return the project name or an empty string if the name could not be
     *         resolved
     */
    private String parsePom(final String fileName) {
        try {
            InputStream pom = factory.create(fileName);
            Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(ModuleDetector.class.getClassLoader());

            digester.push(new StringBuffer());
            digester.addCallMethod("project/name", "append", 0);

            StringBuffer result = (StringBuffer)digester.parse(pom);
            return result.toString();
        }
        catch (IOException exception) {
            // ignore
        }
        catch (SAXException exception) {
            // ignore
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

