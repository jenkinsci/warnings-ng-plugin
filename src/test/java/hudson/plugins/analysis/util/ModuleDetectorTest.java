package hudson.plugins.analysis.util;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;

/**
 *  Tests the class {@link ModuleDetector}.
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"DMI", "OBL", "SIC"})
public class ModuleDetectorTest {
    /** Prefix of the path in test. */
    private static final String PATH_PREFIX = "/path/to/";
    /** Expected module name for all tests. */
    private static final String EXPECTED_MODULE = "com.avaloq.adt.core";
    /** JUnit Error message. */
    private static final String ERROR_MESSAGE = "Wrong module name detected.";

    /**
     * Checks whether we could identify a name from the file name.
     */
    @Test
    public void testTopLevelModuleName() {
        ModuleDetector detector = new ModuleDetector();

        String moduleName = detector.guessModuleName("com.avaloq.adt.core/pmd.xml", false, false);
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
        moduleName = detector.guessModuleName("com.avaloq.adt.core\\pmd.xml", false, false);
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
        InputStream pom = ModuleDetectorTest.class.getResourceAsStream(ModuleDetector.MAVEN_POM);
        expect(factory.create(isA(String.class))).andReturn(pom);

        ModuleDetector detector = new ModuleDetector();
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "ADT Business Logic", detector.guessModuleName("prefix/target/suffix", true, false));

        verify(factory);
    }

    /**
     * Checks whether we could identify maven modules using the module mapping.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("DMI")
    public void testPomModules() throws FileNotFoundException {
        FileInputStreamFactory factory = createMock(FileInputStreamFactory.class);
        InputStream pom = ModuleDetectorTest.class.getResourceAsStream(ModuleDetector.MAVEN_POM);
        expect(factory.create(isA(String.class))).andReturn(pom);
        replay(factory);

        ModuleDetector detector = new ModuleDetector(new File("/"), factory) {
            /** {@inheritDoc} */
            @Override
            protected String[] find(final File path, final String pattern) {
                return new String[] {PATH_PREFIX + MAVEN_POM};
            }
        };

        assertEquals("Wrong module guessed", "ADT Business Logic", detector.guessModuleName(PATH_PREFIX));

        verify(factory);
    }

    /**
     * Checks whether we could identify ant projects using the module mapping.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testAntModules() throws FileNotFoundException {
        FileInputStreamFactory factory = createMock(FileInputStreamFactory.class);
        InputStream pom = ModuleDetectorTest.class.getResourceAsStream(ModuleDetector.ANT_PROJECT);
        expect(factory.create(isA(String.class))).andReturn(pom);
        replay(factory);

        ModuleDetector detector = new ModuleDetector(new File("/"), factory) {
            /** {@inheritDoc} */
            @Override
            protected String[] find(final File path, final String pattern) {
                return new String[] {PATH_PREFIX + ModuleDetector.ANT_PROJECT};
            }
        };

        assertEquals("Wrong number of elements in mapping", "checkstyle", detector.guessModuleName(PATH_PREFIX));

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
        InputStream buildXml = ModuleDetectorTest.class.getResourceAsStream(ModuleDetector.ANT_PROJECT);
        expect(factory.create(isA(String.class))).andReturn(buildXml);

        ModuleDetector detector = new ModuleDetector();
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "checkstyle", detector.guessModuleName("prefix/checkstyle.xml", false, true));

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
        InputStream buildXml = ModuleDetectorTest.class.getResourceAsStream(ModuleDetector.ANT_PROJECT);
        expect(factory.create(isA(String.class))).andReturn(buildXml);

        ModuleDetector detector = new ModuleDetector();
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "checkstyle", detector.guessModuleName("checkstyle.xml", false, true));

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

        ModuleDetector detector = new ModuleDetector();
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "prefix", detector.guessModuleName("prefix/suffix", true, false));

        verify(factory);
    }

    /**
     * Checks whether we return the folder before the filename if there is no pom or folder match.
     */
    @Test
    public void testNoGuess() {
        ModuleDetector detector = new ModuleDetector();

        String moduleName = detector.guessModuleName("base/com.hello.world/com.avaloq.adt.core/pmd.xml", false, false);
        assertEquals(ERROR_MESSAGE, "com.avaloq.adt.core", moduleName);

        moduleName = detector.guessModuleName("com.avaloq.adt.core/pmd.xml", false, false);
        assertEquals(ERROR_MESSAGE, "com.avaloq.adt.core", moduleName);
    }
}
