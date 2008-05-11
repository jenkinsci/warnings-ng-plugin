package hudson.plugins.warnings.util;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 *  Tests the class {@link MavenModuleDetector}.
 */
public class MavenModuleDetectorTest {
    /** Expected module name for all tests. */
    private static final String EXPECTED_MODULE = "com.avaloq.adt.core";
    /** JUnit Error message. */
    private static final String ERROR_MESSAGE = "Wrong module name detected.";
    /** Detector under test. */
    private final MavenModuleDetector detector = new MavenModuleDetector();

    /**
     * Checks whether we could identify a maven module name from a top level module.
     */
    @Test
    public void testTopLevelModuleName() {
        String moduleName = detector.guessModuleName("com.avaloq.adt.core/src/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
        moduleName = detector.guessModuleName("com.avaloq.adt.core/target/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
        moduleName = detector.guessModuleName("com.avaloq.adt.core\\src\\com\\avaloq\\adt\\core\\job\\AvaloqJob.java");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
    }

    /**
     * Checks whether we could identify a maven module name from a sub module.
     */
    @Test
    public void testSubModuleName() {
        String moduleName = detector.guessModuleName("base/com.hello.world/com.avaloq.adt.core/src/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
    }

    /**
     * Checks whether we could identify a maven module regardless of filename conventions.
     */
    @Test
    public void testTargetName() {
        String moduleName = detector.guessModuleName("X:\\Build\\Results\\jobs\\ADT-Base\\workspace\\com.avaloq.adt.core\\target\\pmd.xml");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);

        String input = "workspace/com.avaloq.adt.core/target/findbugs.xml";
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, detector.guessModuleName(input));

        input = "com.avaloq.adt.core/target/findbugs.xml";
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, detector.guessModuleName(input));

        input = "X:\\work\\workspace\\com.avaloq.adt.core\\target\\findbugs.xml";
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, detector.guessModuleName(input));

        input = "com.avaloq.adt.core\\target\\findbugs.xml";
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, detector.guessModuleName(input));

        input = "com.avaloq.adt.core\\findbugs.xml";
        assertEquals(ERROR_MESSAGE, "", detector.guessModuleName(input));
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
        InputStream pom = MavenModuleDetectorTest.class.getResourceAsStream("pom.xml");
        expect(factory.create(isA(String.class))).andReturn(pom);
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "ADT Business Logic", detector.guessModuleName("prefix/target/suffix"));

        verify(factory);
    }

    /**
     * Checks whether we could identify a maven module from a POM using the source folder.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testPomNameOnSrc() throws FileNotFoundException {
        FileInputStreamFactory factory = createMock(FileInputStreamFactory.class);
        InputStream pom = MavenModuleDetectorTest.class.getResourceAsStream("pom.xml");
        expect(factory.create(isA(String.class))).andReturn(pom);
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "ADT Business Logic", detector.guessModuleName("prefix/src/suffix"));

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
        expect(factory.create(isA(String.class))).andThrow(new FileNotFoundException());
        detector.setFileInputStreamFactory(factory);

        replay(factory);

        assertEquals(ERROR_MESSAGE, "prefix", detector.guessModuleName("prefix/src/suffix"));

        verify(factory);
    }

    /**
     * Checks whether we return an empty string if we can't guess the module name.
     */
    @Test
    public void testEmptyString() {
        String moduleName = detector.guessModuleName("base/com.hello.world/com.avaloq.adt.core/source/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals(ERROR_MESSAGE, StringUtils.EMPTY, moduleName);
    }
}
