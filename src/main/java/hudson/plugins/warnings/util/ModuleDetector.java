package hudson.plugins.warnings.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Detects module names by parsing the name of a source file, the Maven pom.xml file or the ANT build.xml file.
 *
 * @author Ulli Hafner
 */
public final class ModuleDetector {
    /** Prefix of a maven target folder. */
    private static final String TARGET = "/target";
    /** The factory to create input streams with. */
    private FileInputStreamFactory factory = new DefaultFileInputStreamFactory();

    /**
     * Sets the factory to the specified value.
     *
     * @param fileInputStreamFactory the value to set
     */
    public void setFileInputStreamFactory(final FileInputStreamFactory fileInputStreamFactory) {
        factory = fileInputStreamFactory;
    }

    /**
     * Guesses a maven module name based on the source folder.
     *
     * @param fileName
     *            the absolute path of the file (UNIX style) to guess the module
     *            for
     * @return the guessed module name or an empty string if the name could not be
     *         resolved
     */
    public String guessModuleName(final String fileName) {
        String unixName = fileName.replace("\\", "/");

        String projectName = parsePom(unixName);
        if (StringUtils.isNotBlank(projectName)) {
            return projectName;
        }

        String path = StringUtils.substringBeforeLast(unixName, "/");
        projectName = parseBuildXml(path);
        if (StringUtils.isNotBlank(projectName)) {
            return projectName;
        }

        if (path.contains("/")) {
            return StringUtils.substringAfterLast(path, "/");
        }
        else {
            return path;
        }
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
            String fileName;
            if (StringUtils.isBlank(path)) {
                fileName = "build.xml";
            }
            else {
                fileName = path + "/build.xml";
            }
            InputStream pom = factory.create(fileName);

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
     *            maven module root folder
     * @return the project name or an empty string if the name could not be
     *         resolved
     */
    private String parsePom(final String fileName) {
        try {
            if (fileName.contains(TARGET)) {
                String module = StringUtils.substringBeforeLast(fileName, TARGET);
                InputStream pom = factory.create(module + "/pom.xml");

                Digester digester = new Digester();
                digester.setValidating(false);
                digester.setClassLoader(ModuleDetector.class.getClassLoader());

                digester.push(new StringBuffer());
                digester.addCallMethod("project/name", "append", 0);

                StringBuffer result = (StringBuffer)digester.parse(pom);
                return result.toString();
            }
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
     * A input stream factory based on a {@link FileInputStream}.
     */
    private static final class DefaultFileInputStreamFactory implements FileInputStreamFactory {
        /** {@inheritDoc} */
        public InputStream create(final String fileName) throws FileNotFoundException {
            return new FileInputStream(new File(fileName));
        }
    }
}

