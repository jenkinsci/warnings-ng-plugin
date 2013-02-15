package hudson.plugins.warnings.parser;

import hudson.XmlFile;
import hudson.plugins.analysis.test.AbstractSerializeModelTest;
import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.AnnotationStream;
import hudson.plugins.analysis.util.model.JavaProject;
import hudson.plugins.analysis.util.model.Priority;

import java.io.File;

import org.junit.Assert;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

/**
 * Tests the serialization of the model.
 *
 * @see <a href="http://www.ibm.com/developerworks/library/j-serialtest.html">Testing object serialization</a>
 */
public class BugSerializeModelTest extends AbstractSerializeModelTest {
    /** Serialization provider. */
    private static final XStream XSTREAM = new AnnotationStream();

    static {
        XSTREAM.alias("warning", Warning.class);
    }

    /**
     * Verifies the first created annotation.
     *
     * @param annotation
     *            the first created annotation
     */
    @Override
    protected void verifyFirstAnnotation(final AbstractAnnotation annotation) {
        Warning bug = (Warning)annotation;
        Assert.assertEquals("Wrong detail message." , TEST_TASK1, bug.getMessage());
    }

    /**
     * Creates an annotation.
     *
     * @param line
     *            the line
     * @param message
     *            the message
     * @param priority
     *            the priority
     * @param fileName
     *            the file name
     * @param packageName
     *            the package name
     * @param moduleName
     *            the module name
     * @return the annotation
     */
    @Override
    protected AbstractAnnotation createAnnotation(final int line, final String message, final Priority priority, final String fileName, final String packageName, final String moduleName) {
        Warning annotation = new Warning(message, line, message, message, message, priority);
        annotation.setFileName(fileName);
        annotation.setPackageName(packageName);
        annotation.setModuleName(moduleName);
        return annotation;
    }

    /**
     * Test whether a serialized project is the same object after
     * deserialization of the file format of release 2.2.
     */
    @Test
    public void ensureSameSerialization() {
        JavaProject project = deserialize("project.ser");

        verifyProject(project);
    }

    /**
     * Test whether a serialized project is the same object after
     * deserialization of the file format of release 2.2.
     */
    @Test
    public void ensureSameXmlSerialization() {
        ensureSerialization("project.ser.xml");
    }

    @Override
    protected XmlFile createXmlFile(final File file) {
        return new XmlFile(XSTREAM, file);
    }
}

