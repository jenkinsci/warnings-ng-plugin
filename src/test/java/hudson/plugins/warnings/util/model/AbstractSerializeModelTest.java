package hudson.plugins.warnings.util.model;

import static junit.framework.Assert.*;
import hudson.XmlFile;
import hudson.plugins.warnings.util.AbstractEnglishLocaleTest;
import hudson.plugins.warnings.util.Messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Tests the serialization of the model.
 *
 * @see <a href="http://www.ibm.com/developerworks/library/j-serialtest.html">Testing object serialization</a>
 */
public abstract class AbstractSerializeModelTest extends AbstractEnglishLocaleTest {
    /** Task property. */
    protected static final String MODULE2 = "Module2";
    /** Task property. */
    protected static final String MODULE1 = "Module1";
    /** Task property. */
    protected static final String PACKAGE2 = "Package2";
    /** Task property. */
    protected static final String PACKAGE1 = "Package1";
    /** Task property. */
    protected static final String PATH_TO_FILE2 = "Path/To/File2";
    /** Task property. */
    protected static final String PATH_TO_FILE1 = "Path/To/File";
    /** Short file name. */
    private static final String FILE1 = StringUtils.substringAfterLast(PATH_TO_FILE1, "/");
    /** Task property. */
    protected static final String LOW = "Low";
    /** Task property. */
    protected static final String NORMAL = "NORMAL";
    /** Task property. */
    protected static final String HIGH = "high";
    /** Task property. */
    protected static final String TEST_TASK5 = "Test Task5";
    /** Task property. */
    protected static final String TEST_TASK4 = "Test Task4";
    /** Task property. */
    protected static final String TEST_TASK3 = "Test Task3";
    /** Task property. */
    protected static final String TEST_TASK2 = "Test Task2";
    /** Task property. */
    protected static final String TEST_TASK1 = "Test Task1";
    /** Task property. */
    protected static final int LINE_NUMBER = 100;
    /** Error Message. */
    private static final String WRONG_PACKAGE_NAME = "Wrong package name.";
    /** Error Message. */
    private static final String WRONG_TOOLTIP_CREATED = "Wrong tooltip created.";
    /** Error Message. */
    private static final String WRONG_NUMBER_OF_FILES = "Wrong number of files.";
    /** Error Message. */
    private static final String WRONG_NUMBER_OF_PACKAGES = "Wrong number of packages.";
    /** Error Message. */
    private static final String WRONG_NUMBER_OF_MODULES = "Wrong number of modules.";
    /** Error Message. */
    private static final String WRONG_NUMBER_OF_ANNOTATIONS = "Wrong number of annotations.";
    /** Error Message. */
    private static final String WRONG_MODULE_ERROR = "Wrong module error.";
    /** Error Message. */
    private static final String WRONG_ANNOTATION_KEY = "Wrong annotation key.";
    /** Error Message. */
    private static final String WRONG_MODULE_NAME = "Wrong module name.";
    /** Error Message. */
    private static final String WRONG_FILE_SHORT_NAME = "Wrong file short name";
    /** Error Message. */
    private static final String WRONG_FILE_NAME = "Wrong file name.";
    /** Error Message. */
    private static final String MODULE_NOT_IN_PROJECT = "Module not in project.";
    /** Error Message. */
    private static final String PACKAGE_NOT_IN_MODULE = "Package not in module.";

    /** The fist created annotation. */
    private AbstractAnnotation firstAnnotation;

    /**
     * Creates the original object that will be serialized.
     *
     * @return the annotation container
     */
    private JavaProject createOriginal() {
        JavaProject project = new JavaProject();

        addAnnotation(project, LINE_NUMBER, TEST_TASK1, Priority.HIGH, PATH_TO_FILE1, PACKAGE1, MODULE1);
        addAnnotation(project, LINE_NUMBER, TEST_TASK2, Priority.LOW, PATH_TO_FILE1, PACKAGE1, MODULE1);
        addAnnotation(project, LINE_NUMBER, TEST_TASK3, Priority.LOW, PATH_TO_FILE2, PACKAGE1, MODULE1);

        addAnnotation(project, LINE_NUMBER, TEST_TASK4, Priority.NORMAL, PATH_TO_FILE1, PACKAGE2, MODULE1);

        addAnnotation(project, LINE_NUMBER, TEST_TASK5, Priority.NORMAL, PATH_TO_FILE1, PACKAGE1, MODULE2);

        verifyProject(project);
        verifyFirstAnnotation(project);

//        try {
//            OutputStream fout = new FileOutputStream("/project.ser");
//            ObjectOutputStream out = new ObjectOutputStream(fout);
//
//            out.writeObject(project);
//            out.flush();
//            out.close();
//        }
//        catch (FileNotFoundException exception) {
//            // ignore
//        }
//        catch (IOException exception) {
//            // ignore
//        }

        return project;
    }

    /**
     * Verifies the first annotation of the project.
     *
     * @param project the created project
     */
    @SuppressWarnings("PMD")
    protected void verifyFirstAnnotation(final JavaProject project) {
        FileAnnotation annotation = project.getAnnotation(firstAnnotation.getKey());
        assertEquals(WRONG_ANNOTATION_KEY, firstAnnotation, annotation);
        annotation = project.getAnnotation(String.valueOf(firstAnnotation.getKey()));
        assertEquals(WRONG_ANNOTATION_KEY, firstAnnotation, annotation);

        verifyFirstAnnotation(firstAnnotation);

        JavaProject dummyProject = new JavaProject();
        addAnnotation(dummyProject, LINE_NUMBER, TEST_TASK1, Priority.HIGH, PATH_TO_FILE1, PACKAGE1, MODULE1);
        FileAnnotation other = dummyProject.getAnnotations().iterator().next();

        assertEquals("Wrong equals evaluation.", annotation, other);
    }

    /**
     * Verifies the first created annotation.
     *
     * @param annotation
     *            the first created annotation
     */
    protected abstract void verifyFirstAnnotation(final AbstractAnnotation annotation);

    /**
     * Adds a new tasks to the specified project.
     *
     * @param project
     *            the project to add the tasks to
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
     */
    private void addAnnotation(final JavaProject project, final int line, final String message, final Priority priority, final String fileName, final String packageName, final String moduleName) {
        AbstractAnnotation task = createAnnotation(line, message, priority, fileName, packageName, moduleName);
        if (firstAnnotation == null) {
            firstAnnotation = task;
        }
        project.addAnnotation(task);
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
    protected abstract AbstractAnnotation createAnnotation(final int line, final String message, final Priority priority, final String fileName, final String packageName, final String moduleName);

    /**
     * Test whether a serialized task is the same object after deserialization.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testObjectIsSameAfterDeserialization() throws IOException, ClassNotFoundException {
        JavaProject original = createOriginal();
//        Collection<FileAnnotation> files = original.getAnnotations();
//        createXmlFile(new File("/project.ser.xml")).write(files.toArray(new FileAnnotation[files.size()]));

        ByteArrayOutputStream outputStream = serialize(original);
        JavaProject copy = deserialize(outputStream.toByteArray());

        verifyProject(copy);
        verifyFirstAnnotation(copy);
    }

    /**
     * Creates the XML serialization file.
     *
     * @param file the file for the XML data
     * @return the XML serialization file
     */
    protected abstract XmlFile createXmlFile(final File file);

    /**
     * Deserializes an object from the specified data and returns it.
     *
     * @param objectData
     *            the serialized object in plain bytes
     * @return the deserialized object
     * @throws IOException
     *             in case of an IO error
     * @throws ClassNotFoundException
     *             if the wrong class is created
     */
    private JavaProject deserialize(final byte[] objectData) throws IOException, ClassNotFoundException {
       InputStream inputStream = new ByteArrayInputStream(objectData);
       ObjectInputStream objectStream = new ObjectInputStream(inputStream);
       Object readObject = objectStream.readObject();

       return (JavaProject) readObject;
    }

    /**
     * Serializes the specified object and returns the created output stream.
     *
     * @param original
     *            original object
     * @return created output stream
     * @throws IOException
     */
    private ByteArrayOutputStream serialize(final JavaProject original) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
        objectStream.writeObject(original);
        objectStream.close();

        return outputStream;
    }
    /**
     * Verifies the created project.
     *
     * @param project the created project
     */
    @SuppressWarnings("PMD")
    protected void verifyProject(final JavaProject project) {
        assertTrue(project.hasAnnotations());

        checkSummary(project);

        assertTrue(MODULE_NOT_IN_PROJECT, project.containsModule(MODULE1));
        assertTrue(MODULE_NOT_IN_PROJECT, project.containsModule(MODULE2));

        checkFirstModule(project.getModule(MODULE1));
        checkSecondModule(project.getModule(MODULE2));

        for (FileAnnotation annotation : project.getModule(MODULE2).getAnnotations()) {
            assertEquals("Wrong primary line number.", LINE_NUMBER, annotation.getPrimaryLineNumber());
            Collection<LineRange> lineRanges = annotation.getLineRanges();
            assertEquals("Wrong number of ranges.", 1, lineRanges.size());
            LineRange range = lineRanges.iterator().next();
            assertEquals("Wrong start line number.", LINE_NUMBER, range.getStart());
            assertEquals("Wrong end line number.", LINE_NUMBER, range.getEnd());
            assertEquals("Wrong package prefix.", Messages.PackageDetail_header(), project.getModule(MODULE2).getPackageCategoryName());
            assertSame(annotation, project.getAnnotation(annotation.getKey()));
            assertSame(annotation, project.getAnnotation(Long.toString(annotation.getKey())));
        }
    }

    /**
     * Checks the second module of the project.
     *
     * @param module
     *      the module to check
     */
    private void checkSecondModule(final MavenModule module) {
        assertNull(WRONG_MODULE_ERROR, module.getError());
        assertEquals(WRONG_MODULE_NAME, MODULE2, module.getName());

        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getAnnotations().size());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, module.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, module.getAnnotations(Priority.HIGH).size());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getAnnotations(Priority.NORMAL).size());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, module.getNumberOfAnnotations(Priority.LOW));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, module.getAnnotations(Priority.LOW).size());
        assertEquals(WRONG_TOOLTIP_CREATED, "Normal:1", module.getToolTip());

        assertEquals(WRONG_NUMBER_OF_PACKAGES, 1, module.getPackages().size());
        assertEquals(WRONG_NUMBER_OF_FILES, 1, module.getFiles().size());

        assertTrue(PACKAGE_NOT_IN_MODULE, module.containsPackage(PACKAGE1));
        assertFalse("Package in module.", module.containsPackage(PACKAGE2));
        JavaPackage javaPackage = module.getPackage(PACKAGE1);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.LOW));

        WorkspaceFile file = module.getFile(PATH_TO_FILE1);
        assertEquals(WRONG_FILE_NAME, PATH_TO_FILE1, file.getName());
        assertEquals(WRONG_FILE_SHORT_NAME, FILE1, file.getShortName());
        assertTrue(WRONG_NUMBER_OF_ANNOTATIONS, file.hasAnnotations());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, file.getNumberOfAnnotations());
        assertFalse(WRONG_NUMBER_OF_ANNOTATIONS, file.hasAnnotations(HIGH));
        assertFalse(WRONG_NUMBER_OF_ANNOTATIONS, file.hasAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, file.getNumberOfAnnotations(Priority.HIGH));
        assertTrue(WRONG_NUMBER_OF_ANNOTATIONS, file.hasAnnotations(NORMAL));
        assertTrue(WRONG_NUMBER_OF_ANNOTATIONS, file.hasAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, file.getNumberOfAnnotations(Priority.NORMAL));
        assertFalse(WRONG_NUMBER_OF_ANNOTATIONS, file.hasAnnotations(LOW));
        assertFalse(WRONG_NUMBER_OF_ANNOTATIONS, file.hasAnnotations(Priority.LOW));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, file.getNumberOfAnnotations(Priority.LOW));
        assertEquals(WRONG_TOOLTIP_CREATED, "Normal:1", file.getToolTip());
    }

    /**
     * Checks the first module of the project.
     *
     * @param module
     *      the module to check
     */
    private void checkFirstModule(final MavenModule module) {
        assertEquals(WRONG_MODULE_NAME, MODULE1, module.getName());
        assertNull(WRONG_MODULE_ERROR, module.getError());

        assertEquals(WRONG_NUMBER_OF_MODULES, 0, module.getModules().size());
        assertEquals(WRONG_NUMBER_OF_PACKAGES, 2, module.getPackages().size());
        assertEquals(WRONG_NUMBER_OF_FILES, 2, module.getFiles().size());

        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 4, module.getNumberOfAnnotations());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, module.getNumberOfAnnotations(Priority.LOW));

        assertTrue(PACKAGE_NOT_IN_MODULE, module.containsPackage(PACKAGE1));
        JavaPackage javaPackage = module.getPackage(PACKAGE1);
        assertEquals(WRONG_PACKAGE_NAME, PACKAGE1, javaPackage.getName());

        assertEquals(WRONG_NUMBER_OF_MODULES, 0, javaPackage.getModules().size());
        assertEquals(WRONG_NUMBER_OF_PACKAGES, 0, javaPackage.getPackages().size());
        assertEquals(WRONG_NUMBER_OF_FILES, 2, javaPackage.getFiles().size());

        assertEquals(WRONG_NUMBER_OF_FILES, 2, javaPackage.getFiles().size());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 3, javaPackage.getNumberOfAnnotations());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, javaPackage.getNumberOfAnnotations(Priority.LOW));

        assertTrue("File not in package.", javaPackage.containsFile(PATH_TO_FILE1));
        WorkspaceFile file = javaPackage.getFile(PATH_TO_FILE1);
        assertEquals(WRONG_FILE_NAME, PATH_TO_FILE1, file.getName());
        assertEquals(WRONG_FILE_SHORT_NAME, FILE1, file.getShortName());

        assertEquals(WRONG_NUMBER_OF_MODULES, 0, file.getModules().size());
        assertEquals(WRONG_NUMBER_OF_PACKAGES, 0, file.getPackages().size());
        assertEquals(WRONG_NUMBER_OF_FILES, 0, file.getFiles().size());

        javaPackage = module.getPackage(PACKAGE2);
        assertEquals(WRONG_PACKAGE_NAME, PACKAGE2, javaPackage.getName());
        assertEquals(WRONG_NUMBER_OF_FILES, 1, javaPackage.getFiles().size());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations());
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.LOW));
    }

    /**
     * Checks the summary information of the project.
     *
     * @param project
     *      the project to check
     */
    private void checkSummary(final JavaProject project) {
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 5, project.getNumberOfAnnotations());
        assertTrue(WRONG_NUMBER_OF_ANNOTATIONS, project.hasAnnotations(Priority.HIGH));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, project.getNumberOfAnnotations(Priority.HIGH));
        assertTrue(WRONG_NUMBER_OF_ANNOTATIONS, project.hasAnnotations(Priority.NORMAL));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, project.getNumberOfAnnotations(Priority.NORMAL));
        assertTrue(WRONG_NUMBER_OF_ANNOTATIONS, project.hasAnnotations(Priority.LOW));
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, project.getNumberOfAnnotations(Priority.LOW));
        assertEquals(WRONG_TOOLTIP_CREATED, "High:1 - Normal:2 - Low:2", project.getToolTip());

        assertEquals(WRONG_NUMBER_OF_MODULES, 2, project.getModules().size());
        assertEquals(WRONG_NUMBER_OF_PACKAGES, 2, project.getPackages().size());
        assertEquals(WRONG_NUMBER_OF_FILES, 2, project.getFiles().size());
    }
}

