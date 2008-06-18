package hudson.plugins.warnings.util;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;

/**
 *  Tests the class {@link ModuleDetector}.
 */
public class ModuleDetectorTest {
    /** Expected module name for all tests. */
    private static final String EXPECTED_MODULE = "com.avaloq.adt.core";
    /** JUnit Error message. */
    private static final String ERROR_MESSAGE = "Wrong module name detected.";
    /** Detector under test. */
    private final ModuleDetector detector = new ModuleDetector();

    /**
     * Checks whether we could identify a name from the file name.
     */
    @Test
    public void testTopLevelModuleName() {
        String moduleName = detector.guessModuleName("com.avaloq.adt.core/pmd.xml");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
        moduleName = detector.guessModuleName("com.avaloq.adt.core\\pmd.xml");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
    }

    /**
     * Checks whether we could identify a maven module from a POM using the target folder.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testPomNameOnTarget() throws FileNotFoundException {
        FileInputStreamFactory factory = createMock(FileInputStreamFactory.class);
        InputStream pom = ModuleDetectorTest.class.getResourceAsStream("pom.xml");
        expect(factory.create(isA(String.class))).andReturn(pom);
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "ADT Business Logic", detector.guessModuleName("prefix/target/suffix"));

        verify(factory);
    }

    /**
     * Checks whether we could identify a ANT project name from a build.xml file.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testProjectName() throws FileNotFoundException {
        FileInputStreamFactory factory = createMock(FileInputStreamFactory.class);
        InputStream buildXml = ModuleDetectorTest.class.getResourceAsStream("build.xml");
        expect(factory.create(isA(String.class))).andReturn(buildXml);
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "checkstyle", detector.guessModuleName("prefix/checkstyle.xml"));

        verify(factory);
    }

    /**
     * Checks whether we could identify a ANT project name from a build.xml file on the root.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testProjectNameNoPath() throws FileNotFoundException {
        FileInputStreamFactory factory = createMock(FileInputStreamFactory.class);
        InputStream buildXml = ModuleDetectorTest.class.getResourceAsStream("build.xml");
        expect(factory.create(isA(String.class))).andReturn(buildXml);
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "checkstyle", detector.guessModuleName("checkstyle.xml"));

        verify(factory);
    }

    /**
     * Checks whether we could identify a java package name and maven module.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testNoPomNameOnException() throws FileNotFoundException {
        FileInputStreamFactory factory = createMock(FileInputStreamFactory.class);
        expect(factory.create(isA(String.class))).andThrow(new FileNotFoundException()).anyTimes();
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "prefix", detector.guessModuleName("prefix/suffix"));

        verify(factory);
    }

    /**
     * Checks whether we return the folder before the filename if there is no pom or folder match.
     */
    @Test
    public void testNoGuess() {
        String moduleName = detector.guessModuleName("base/com.hello.world/com.avaloq.adt.core/pmd.xml");
        assertEquals(ERROR_MESSAGE, "com.avaloq.adt.core", moduleName);

        moduleName = detector.guessModuleName("com.avaloq.adt.core/pmd.xml");
        assertEquals(ERROR_MESSAGE, "com.avaloq.adt.core", moduleName);
    }
}
