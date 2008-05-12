package hudson.plugins.warnings.util.model;

import hudson.XmlFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the serialization of the model.
 *
 * @see <a href="http://www.ibm.com/developerworks/library/j-serialtest.html">Testing object serialization</a>
 */
public abstract class AbstractSerializeModelTest {
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
    private static final String WRONG_MAXIMUM_NUMBER = "Wrong maximum number of annotations per package.";
    /** Error Message. */
    private static final String WRONG_NUMBER_OF_ANNOTATIONS = "Wrong number of annotations.";
    /** Error Message. */
    private static final String WRONG_MODULE_ERROR = "Wrong module error.";
    /** Error Message. */
    private static final String WRONG_MODULE_NAME = "Wrong module name.";
    /** Error Message. */
    private static final String WRONG_ANNOTATION_KEY = "Wrong annotation key.";

    /** The fist created annotation. */
    private AbstractAnnotation firstAnnotation;

    /**
     * Initializes the locale to English.
     */
    @Before
    public void initializeLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    /**
     * Creates the original object that will be serialized.
     *
     * @return the annotation container
     */
    private JavaProject createOriginal() {
        final JavaProject project = new JavaProject();

        addAnnotation(project, LINE_NUMBER, TEST_TASK1, Priority.HIGH, PATH_TO_FILE1, PACKAGE1, MODULE1);
        addAnnotation(project, LINE_NUMBER, TEST_TASK2, Priority.LOW, PATH_TO_FILE1, PACKAGE1, MODULE1);
        addAnnotation(project, LINE_NUMBER, TEST_TASK3, Priority.LOW, PATH_TO_FILE2, PACKAGE1, MODULE1);

        addAnnotation(project, LINE_NUMBER, TEST_TASK4, Priority.NORMAL, PATH_TO_FILE1, PACKAGE2, MODULE1);

        addAnnotation(project, LINE_NUMBER, TEST_TASK5, Priority.NORMAL, PATH_TO_FILE1, PACKAGE1, MODULE2);

        verifyProject(project);
        verifyFirstAnnotation(project);

//        try {
//            OutputStream fout = new FileOutputStream("project.ser");
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
        Assert.assertEquals(WRONG_ANNOTATION_KEY, firstAnnotation, annotation);
        annotation = project.getAnnotation(String.valueOf(firstAnnotation.getKey()));
        Assert.assertEquals(WRONG_ANNOTATION_KEY, firstAnnotation, annotation);

        verifyFirstAnnotation(firstAnnotation);

        JavaProject dummyProject = new JavaProject();
        addAnnotation(dummyProject, LINE_NUMBER, TEST_TASK1, Priority.HIGH, PATH_TO_FILE1, PACKAGE1, MODULE1);
        FileAnnotation other = dummyProject.getAnnotations().iterator().next();

        Assert.assertEquals("Wrong equals evaluation.", annotation, other);
    }

    /**
     * Verifies the created project.
     *
     * @param project the created project
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD")
    protected void verifyProject(final JavaProject project) {
        Assert.assertTrue(project.hasAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 5, project.getNumberOfAnnotations());
        Assert.assertTrue(project.hasAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, project.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertTrue(project.hasAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, project.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertTrue(project.hasAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, project.getNumberOfAnnotations(Priority.LOW));
        Assert.assertEquals("Wrong maximum number of annotations per module.", 4, project.getAnnotationBound());
        Assert.assertEquals(WRONG_TOOLTIP_CREATED, "High:1 - Normal:2 - Low:2", project.getToolTip());

        Assert.assertEquals(WRONG_NUMBER_OF_MODULES, 2, project.getModules().size());
        Assert.assertEquals(WRONG_NUMBER_OF_PACKAGES, 3, project.getPackages().size());
        Assert.assertEquals(WRONG_NUMBER_OF_FILES, 4, project.getFiles().size());

        MavenModule module = project.getModule(MODULE1);
        Assert.assertEquals(WRONG_MODULE_NAME, MODULE1, module.getName());

        Assert.assertTrue(module.hasAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 4, module.getNumberOfAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 4, module.getAnnotations().size());
        Assert.assertTrue(module.hasAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations(HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getAnnotations(Priority.HIGH).size());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getAnnotations(HIGH).size());
        Assert.assertTrue(module.hasAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations(NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getAnnotations(Priority.NORMAL).size());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getAnnotations(NORMAL).size());
        Assert.assertTrue(module.hasAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, module.getNumberOfAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, module.getNumberOfAnnotations(LOW));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, module.getAnnotations(Priority.LOW).size());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, module.getAnnotations(LOW).size());
        Assert.assertEquals(WRONG_MAXIMUM_NUMBER, 3, module.getAnnotationBound());
        Assert.assertEquals(WRONG_TOOLTIP_CREATED, "High:1 - Normal:1 - Low:2", module.getToolTip());

        Assert.assertEquals(WRONG_NUMBER_OF_FILES, 3, module.getFiles().size());
        Assert.assertEquals(WRONG_NUMBER_OF_PACKAGES, 2, module.getPackages().size());

        JavaPackage javaPackage = module.getPackage(PACKAGE1);
        Assert.assertEquals(WRONG_PACKAGE_NAME, PACKAGE1, javaPackage.getName());
        Assert.assertTrue(javaPackage.hasAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 3, javaPackage.getNumberOfAnnotations());
        Assert.assertTrue(javaPackage.hasAnnotations(HIGH));
        Assert.assertTrue(javaPackage.hasAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertFalse(javaPackage.hasAnnotations(NORMAL));
        Assert.assertFalse(javaPackage.hasAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertTrue(javaPackage.hasAnnotations(LOW));
        Assert.assertTrue(javaPackage.hasAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, javaPackage.getNumberOfAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_TOOLTIP_CREATED, "High:1 - Low:2", javaPackage.getToolTip());

        WorkspaceFile file = javaPackage.getFile(PATH_TO_FILE1);
        Assert.assertEquals("Wrong file name.", PATH_TO_FILE1, file.getName());
        Assert.assertEquals("Wrong short file name.", "File", file.getShortName());
        Assert.assertTrue(file.hasAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 2, file.getNumberOfAnnotations());
        Assert.assertTrue(file.hasAnnotations(HIGH));
        Assert.assertTrue(file.hasAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, file.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertFalse(file.hasAnnotations(NORMAL));
        Assert.assertFalse(file.hasAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, file.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertTrue(file.hasAnnotations(LOW));
        Assert.assertTrue(file.hasAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, file.getNumberOfAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_TOOLTIP_CREATED, "High:1 - Low:1", file.getToolTip());

        javaPackage = module.getPackage(PACKAGE2);
        Assert.assertEquals(WRONG_PACKAGE_NAME, PACKAGE2, javaPackage.getName());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.LOW));

        module = project.getModule(MODULE2);
        Assert.assertNull(WRONG_MODULE_ERROR, module.getError());
        Assert.assertEquals(WRONG_MODULE_NAME, MODULE2, module.getName());

        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, module.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, module.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, module.getNumberOfAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_MAXIMUM_NUMBER, 1, module.getAnnotationBound());
        Assert.assertEquals(WRONG_TOOLTIP_CREATED, "Normal:1", module.getToolTip());

        Assert.assertEquals(WRONG_NUMBER_OF_PACKAGES, 1, module.getPackages().size());
        Assert.assertEquals(WRONG_NUMBER_OF_FILES, 1, module.getFiles().size());

        javaPackage = module.getPackage(PACKAGE1);
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, javaPackage.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, javaPackage.getNumberOfAnnotations(Priority.LOW));

        file = module.getFile(PATH_TO_FILE1);
        Assert.assertEquals("Wrong file name.", PATH_TO_FILE1, file.getName());
        Assert.assertEquals("Wrong short file name.", "File", file.getShortName());
        Assert.assertTrue(file.hasAnnotations());
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, file.getNumberOfAnnotations());
        Assert.assertFalse(file.hasAnnotations(HIGH));
        Assert.assertFalse(file.hasAnnotations(Priority.HIGH));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, file.getNumberOfAnnotations(Priority.HIGH));
        Assert.assertTrue(file.hasAnnotations(NORMAL));
        Assert.assertTrue(file.hasAnnotations(Priority.NORMAL));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 1, file.getNumberOfAnnotations(Priority.NORMAL));
        Assert.assertFalse(file.hasAnnotations(LOW));
        Assert.assertFalse(file.hasAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, file.getNumberOfAnnotations(Priority.LOW));
        Assert.assertEquals(WRONG_TOOLTIP_CREATED, "Normal:1", file.getToolTip());

        for (FileAnnotation annotation : module.getAnnotations()) {
            Assert.assertEquals("Wrong primary line number.", LINE_NUMBER, annotation.getPrimaryLineNumber());
            Collection<LineRange> lineRanges = annotation.getLineRanges();
            Assert.assertEquals("Wrong number of ranges.", 1, lineRanges.size());
            LineRange range = lineRanges.iterator().next();
            Assert.assertEquals("Wrong start line number.", LINE_NUMBER, range.getStart());
            Assert.assertEquals("Wrong end line number.", LINE_NUMBER, range.getEnd());
        }
    }
    // CHECKSTYLE:ON

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
        AbstractAnnotation task = createAnnotation(line, message, priority);
        if (firstAnnotation == null) {
            firstAnnotation = task;
        }
        task.setFileName(fileName);
        task.setPackageName(packageName);
        task.setModuleName(moduleName);
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
     * @return the annotation
     */
    protected abstract AbstractAnnotation createAnnotation(final int line, final String message, final Priority priority);

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
}

