package hudson.plugins.analysis.util;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 *  Tests the class {@link ModuleDetector}.
 */
@SuppressWarnings("DMI")
public class ModuleDetectorTest {
    private static final File ROOT = new File("/tmp");
    /**
     * FIXME: Document field PREFIX
     */
    private static final String PREFIX = ROOT.getAbsolutePath() + "/";
    private static final int NO_RESULT = 0;
    private static final String PATH_PREFIX_MAVEN = "path/to/maven";
    private static final String PATH_PREFIX_ANT = "path/to/ant";
    private static final String EXPECTED_MAVEN_MODULE = "ADT Business Logic";
    private static final String EXPECTED_ANT_MODULE = "checkstyle";

    private ModuleDetector createDetectorUnderTest(final String fileName, final String[] workspaceScanResult) throws FileNotFoundException {
        return createDetectorUnderTest(createFactoryMock(fileName, workspaceScanResult));
    }

    private ModuleDetector createDetectorUnderTest(final FileInputStreamFactory factory) {
        return new ModuleDetector(ROOT, factory);
    }

    @SuppressWarnings("OBL")
    private FileInputStreamFactory createFactoryMock(final String fileName, final String[] workspaceScanResult) throws FileNotFoundException {
        FileInputStreamFactory factory = mock(FileInputStreamFactory.class);
        InputStream inputFile = read(fileName);
        when(factory.create(anyString())).thenReturn(inputFile);
        when(factory.find((File)anyObject(), anyString())).thenReturn(workspaceScanResult);
        return factory;
    }

    private InputStream read(final String fileName) {
        return ModuleDetectorTest.class.getResourceAsStream(fileName);
    }

    /**
     * Checks whether we could identify Maven modules using the module mapping.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testPomModules() throws FileNotFoundException {
        ModuleDetector detector = createDetectorUnderTest(ModuleDetector.MAVEN_POM,
                new String[] {PATH_PREFIX_MAVEN + ModuleDetector.MAVEN_POM});

        verifyModuleName(detector, EXPECTED_MAVEN_MODULE, PATH_PREFIX_MAVEN + "/something.txt");
        verifyModuleName(detector, EXPECTED_MAVEN_MODULE, PATH_PREFIX_MAVEN + "/in/between/something.txt");
        verifyModuleName(detector, StringUtils.EMPTY, "/path/to/something.txt");
    }

    private void verifyModuleName(final ModuleDetector detector, final String expectedName, final String fileName) {
        assertEquals("Wrong module guessed", expectedName, detector.guessModuleName(PREFIX + fileName));
    }

    /**
     * Checks whether we could identify Ant projects using the module mapping.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testAntModules() throws FileNotFoundException {
        ModuleDetector detector = createDetectorUnderTest(ModuleDetector.ANT_PROJECT,
                new String[] {PATH_PREFIX_ANT + ModuleDetector.ANT_PROJECT});

        verifyModuleName(detector, EXPECTED_ANT_MODULE, PATH_PREFIX_ANT + "/something.txt");
        verifyModuleName(detector, EXPECTED_ANT_MODULE, PATH_PREFIX_ANT + "/in/between/something.txt");
        verifyModuleName(detector, StringUtils.EMPTY, "/path/to/something.txt");
    }

    /**
     * Checks whether we ignore exceptions during parsing.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testNoPomNameOnException() throws FileNotFoundException {
        FileInputStreamFactory factory = createDummyFactory();
        ModuleDetector detector = createDetectorUnderTest(factory);

        verifyModuleName(detector, StringUtils.EMPTY, PATH_PREFIX_ANT + "/something.txt");
        verifyModuleName(detector, StringUtils.EMPTY, PATH_PREFIX_MAVEN + "/something.txt");
    }

    private FileInputStreamFactory createDummyFactory() throws FileNotFoundException {
        FileInputStreamFactory factory = mock(FileInputStreamFactory.class);
        when(factory.create(anyString())).thenThrow(new FileNotFoundException());
        when(factory.find((File)anyObject(), anyString())).thenReturn(new String[NO_RESULT]);
        return factory;
    }

    /**
     * Checks whether we could identify a maven module.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testMoreEntries() throws FileNotFoundException {
        String ant = PATH_PREFIX_ANT + ModuleDetector.ANT_PROJECT;
        String maven = PATH_PREFIX_MAVEN + ModuleDetector.MAVEN_POM;

        FileInputStreamFactory factory = mock(FileInputStreamFactory.class);
        when(factory.create(PREFIX + ant)).thenReturn(read(ModuleDetector.ANT_PROJECT));
        when(factory.create(PREFIX + maven)).thenReturn(read(ModuleDetector.MAVEN_POM));

        when(factory.find((File)anyObject(), anyString())).thenReturn(new String[] {ant, maven});
        ModuleDetector detector = createDetectorUnderTest(factory);

        verifyModuleName(detector, EXPECTED_ANT_MODULE, PATH_PREFIX_ANT + "/something.txt");
        verifyModuleName(detector, EXPECTED_MAVEN_MODULE, PATH_PREFIX_MAVEN + "/something.txt");
    }
}
