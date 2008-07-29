package hudson.plugins.warnings.util;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;
import hudson.model.AbstractBuild;
import hudson.plugins.warnings.util.model.FileAnnotation;
import hudson.plugins.warnings.util.model.Priority;

import java.util.GregorianCalendar;

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;

/**
 * Tests the class {@link AnnotationsBuildResult}.
 *
 * @param <T>
 *            type of the result to test
 */
// TODO: add more tests for the remaining part of the warnings indicator
public abstract class AbstractAnnotationsBuildResultTest<T extends AnnotationsBuildResult> {
    /** Current build number. */
    private static final int BUILD_ID = 42;
    /** Error message. */
    private static final String WRONG_ZERO_WARNINGS_SINCE_BUILD_COUNTER = "Wrong zero warnings since build counter.";
    /** Error message. */
    private static final String WRONG_NUMBER_OF_ANNOTATIONS = "Wrong number of annotations.";
    /** Filename of the annotation. */
    private static final String FILENAME = "filename";

    /**
     * Verifies that the zero warnings since build counter is correctly
     * propagated from build to build.
     */
    @Test
    public void checkThatZeroWarningsIsUpdated() {
        AbstractBuild<?, ?> build = createBuild(BUILD_ID);
        ParserResult projectWithoutAnnotations = new ParserResult();

        T result = createBuildResult(build, projectWithoutAnnotations);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, result.getNumberOfAnnotations());
        assertEquals(WRONG_ZERO_WARNINGS_SINCE_BUILD_COUNTER, 0, result.getZeroWarningsSinceBuild());

        ParserResult projectWithAnnotations = createProjectWithWarning();

        T resultWithAnnotations = createBuildResult(build, projectWithAnnotations);

        result = createBuildResult(build, projectWithoutAnnotations, resultWithAnnotations);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, result.getNumberOfAnnotations());
        assertEquals(WRONG_ZERO_WARNINGS_SINCE_BUILD_COUNTER, BUILD_ID, result.getZeroWarningsSinceBuild());

        result = createBuildResult(createBuild(0), projectWithoutAnnotations, result);
        assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, 0, result.getNumberOfAnnotations());
        assertEquals(WRONG_ZERO_WARNINGS_SINCE_BUILD_COUNTER, BUILD_ID, result.getZeroWarningsSinceBuild());
    }

    /**
     * Creates a project that contains a single annotation.
     *
     * @return the created project
     */
    private ParserResult createProjectWithWarning() {
        ParserResult project = new ParserResult();

        FileAnnotation annotation = mock(FileAnnotation.class);
        stub(annotation.getPriority()).toReturn(Priority.HIGH);
        stub(annotation.getFileName()).toReturn(FILENAME);
        project.addAnnotation(annotation);

        return project;
    }

    /**
     * Creates a mock for the build.
     *
     * @param buildNumber
     *            the current build number
     * @return a mock for the build
     */
    private AbstractBuild<?, ?> createBuild(final int buildNumber) {
        AbstractBuild<?, ?> build = mock(AbstractBuild.class);

        stub(build.getTimestamp()).toReturn(new GregorianCalendar());
        stub(build.getRootDir()).toReturn(SystemUtils.getJavaIoTmpDir());
        stub(build.getNumber()).toReturn(buildNumber);

        return build;
    }

    /**
     * Creates the build result under test.
     *
     * @param build
     *            the current build
     * @param project
     *            the project of the current build
     * @return the build result under test
     */
    protected abstract T createBuildResult(AbstractBuild<?, ?> build, ParserResult project);

    /**
     * Creates the build result under test.
     *
     * @param build
     *            the current build
     * @param project
     *            the project of the current build
     * @param previous
     *            the result of the previous build
     * @return the build result under test
     */
    protected abstract T createBuildResult(AbstractBuild<?, ?> build, ParserResult project, T previous);
}

