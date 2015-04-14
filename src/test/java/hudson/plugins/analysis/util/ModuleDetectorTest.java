package hudson.plugins.analysis.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 *  Tests the class {@link ModuleDetector}.
 */
@SuppressWarnings("DMI")
public class ModuleDetectorTest {
    private static final String EXPECTED_OSGI_MODULE = "de.faktorlogik.prototyp";
    private static final String MANIFEST = "MANIFEST.MF";
    private static final String MANIFEST_NAME = "MANIFEST-NAME.MF";
    private static final File ROOT = new File("/tmp");
    private static final String PREFIX = normalizeRoot();

    private static String normalizeRoot() {
        return ROOT.getAbsolutePath().replace("\\", "/") + "/";
    }
    private static final int NO_RESULT = 0;
    private static final String PATH_PREFIX_MAVEN = "path/to/maven";
    private static final String PATH_PREFIX_OSGI = "path/to/osgi";
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
        when(factory.create(anyString()))
                .thenReturn(read(fileName))
                .thenReturn(read(fileName));
        when(factory.find((File)anyObject(), anyString())).thenReturn(workspaceScanResult);
        return factory;
    }

    private InputStream read(final String fileName) {
        return ModuleDetectorTest.class.getResourceAsStream(fileName);
    }

    /**
     * Checks whether we could identify OSGi modules using the module mapping.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testOsgiModules() throws FileNotFoundException {
        FileInputStreamFactory factory = mock(FileInputStreamFactory.class);
        when(factory.create(anyString())).thenReturn(read(MANIFEST));
        when(factory.find((File)anyObject(), anyString())).thenReturn(new String[]{PATH_PREFIX_OSGI + ModuleDetector.OSGI_BUNDLE});
        ModuleDetector detector = createDetectorUnderTest(factory);

        verifyModuleName(detector, EXPECTED_OSGI_MODULE, PATH_PREFIX_OSGI + "/something.txt");
        verifyModuleName(detector, EXPECTED_OSGI_MODULE, PATH_PREFIX_OSGI + "/in/between/something.txt");
        verifyModuleName(detector, StringUtils.EMPTY, "/path/to/something.txt");
    }

    /**
     * Checks whether we could identify OSGi modules using the module mapping.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testOsgiModulesWithVendor() throws FileNotFoundException {
        FileInputStreamFactory factory = createFactoryMock(MANIFEST, new String[] {PATH_PREFIX_OSGI + ModuleDetector.OSGI_BUNDLE});
        when(factory.create(anyString())).thenReturn(read(MANIFEST)).thenReturn(read("l10n.properties"));
        ModuleDetector detector = createDetectorUnderTest(factory);

        String expectedName = "de.faktorlogik.prototyp (My Vendor)";
        verifyModuleName(detector, expectedName, PATH_PREFIX_OSGI + "/something.txt");
        verifyModuleName(detector, expectedName, PATH_PREFIX_OSGI + "/in/between/something.txt");
        verifyModuleName(detector, StringUtils.EMPTY, "/path/to/something.txt");
    }

    /**
     * Checks whether we could identify OSGi modules using the module mapping.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testOsgiModulesWithName() throws FileNotFoundException {
        FileInputStreamFactory factory = createFactoryMock(MANIFEST_NAME, new String[] {PATH_PREFIX_OSGI + ModuleDetector.OSGI_BUNDLE});
        when(factory.create(anyString())).thenReturn(read(MANIFEST_NAME)).thenReturn(read("l10n.properties"));
        ModuleDetector detector = createDetectorUnderTest(factory);

        String expectedName = "My Bundle";
        verifyModuleName(detector, expectedName, PATH_PREFIX_OSGI + "/something.txt");
        verifyModuleName(detector, expectedName, PATH_PREFIX_OSGI + "/in/between/something.txt");
        verifyModuleName(detector, StringUtils.EMPTY, "/path/to/something.txt");
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

    /**
     * Checks whether we could identify Maven modules using the module mapping.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testPomWithoutName() throws FileNotFoundException {
        ModuleDetector detector = createDetectorUnderTest("no-name-pom.xml",
                new String[] {PATH_PREFIX_MAVEN + ModuleDetector.MAVEN_POM});

        String artifactId = "com.avaloq.adt.core";
        verifyModuleName(detector, artifactId, PATH_PREFIX_MAVEN + "/something.txt");
        verifyModuleName(detector, artifactId, PATH_PREFIX_MAVEN + "/in/between/something.txt");
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

    /**
     * Checks whether maven has precedence over ant.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testMavenHasPrecedenceOverAnt() throws FileNotFoundException {
        String prefix = "/prefix/";
        String ant = prefix + ModuleDetector.ANT_PROJECT;
        String maven = prefix + ModuleDetector.MAVEN_POM;

        verifyOrder(prefix, ant, maven, new String[] {ant, maven});
        verifyOrder(prefix, ant, maven, new String[] {maven, ant});
    }

    private void verifyOrder(final String prefix, final String ant, final String maven, final String[] foundFiles)
            throws FileNotFoundException {
        FileInputStreamFactory factory = mock(FileInputStreamFactory.class);
        when(factory.create(ant)).thenReturn(read(ModuleDetector.ANT_PROJECT));
        when(factory.create(maven)).thenReturn(read(ModuleDetector.MAVEN_POM));

        when(factory.find((File)anyObject(), anyString())).thenReturn(foundFiles);
        ModuleDetector detector = createDetectorUnderTest(factory);

        assertEquals("Wrong module guessed", EXPECTED_MAVEN_MODULE,
                detector.guessModuleName(prefix + "/something.txt"));
    }

    /**
     * Checks whether OSGi has precedence over maven and ant.
     *
     * @throws FileNotFoundException
     *             should never happen
     */
    @Test
    public void testOsgiHasPrecedenceOvermavenAndAnt() throws FileNotFoundException {
        String prefix = "/prefix/";
        String ant = prefix + ModuleDetector.ANT_PROJECT;
        String maven = prefix + ModuleDetector.MAVEN_POM;
        String osgi = prefix + ModuleDetector.OSGI_BUNDLE;

        verifyOrder(prefix, ant, maven, osgi, new String[] {ant, maven, osgi});
        verifyOrder(prefix, ant, maven, osgi, new String[] {ant, osgi, maven});
        verifyOrder(prefix, ant, maven, osgi, new String[] {maven, ant, osgi});
        verifyOrder(prefix, ant, maven, osgi, new String[] {maven, osgi, ant});
        verifyOrder(prefix, ant, maven, osgi, new String[] {osgi, ant, maven});
        verifyOrder(prefix, ant, maven, osgi, new String[] {osgi, maven, osgi});
    }

    private void verifyOrder(final String prefix, final String ant, final String maven, final String osgi, final String[] foundFiles)
            throws FileNotFoundException {
        FileInputStreamFactory factory = mock(FileInputStreamFactory.class);
        when(factory.create(ant)).thenReturn(read(ModuleDetector.ANT_PROJECT));
        when(factory.create(maven)).thenReturn(read(ModuleDetector.MAVEN_POM));
        when(factory.create(osgi)).thenReturn(read(MANIFEST));

        when(factory.find((File)anyObject(), anyString())).thenReturn(foundFiles);
        ModuleDetector detector = createDetectorUnderTest(factory);

        assertEquals("Wrong module guessed", EXPECTED_OSGI_MODULE,
                detector.guessModuleName(prefix + "/something.txt"));
    }
}
