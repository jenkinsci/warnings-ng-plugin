package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.Base64;
import org.junit.Assume;
import org.junit.Test;

import static hudson.Functions.isWindows;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.matrix.TextAxis;
import hudson.model.Result;
import hudson.plugins.warnings.AggregatedWarningsResultAction;
import hudson.plugins.warnings.ConsoleParser;
import hudson.plugins.warnings.WarningsPublisher;
import hudson.tasks.Shell;

/**
 * Integration tests of the warnings plug-in in matrix jobs.
 *
 * @author Ullrich Hafner
 */
public class MatrixJobITest extends IntegrationTest {
    private static final String WARNINGS_FILE = "matrix-warnings.txt";

    /**
     * Build a matrix job with three configurations. For each configuration a different set of warnings will be parsed
     * with the same parser (GCC). After the successful build the total number of warnings at the root level should be
     * set to 12 (sum of all three configurations). Moreover, for each configuration the total number of warnings is
     * also verified (4, 6, and 2 warnings).
     *
     * @throws Exception in case of an error
     */
    @Test
    public void shouldCreateIndividualAxisResults() throws Exception {
        Assume.assumeFalse("Test not yet OS independent: requires UNIX commands", isWindows());

        MatrixProject project = j.createProject(MatrixProject.class);
        enableWarnings(project);

        AxisList axis = new AxisList();
        TextAxis userAxis = new TextAxis("user_axis", "one two three");
        axis.add(userAxis);
        project.setAxes(axis);

        project.getBuildersList().add(new Shell(copyResource(WARNINGS_FILE)));
        project.getBuildersList().add(new Shell("cat " + WARNINGS_FILE + "| grep $user_axis"));

        Map<String, Integer> warningsPerAxis = new HashMap<>();
        warningsPerAxis.put("one", 4);
        warningsPerAxis.put("two", 6);
        warningsPerAxis.put("three", 2);

        MatrixBuild build = project.scheduleBuild2(0).get();
        for (MatrixRun run : build.getRuns()) {
            j.assertBuildStatus(Result.SUCCESS, run);

            AggregatedWarningsResultAction action = run.getAction(AggregatedWarningsResultAction.class);

            assertThat(action).isNotNull();
            String currentAxis = run.getBuildVariables().values().iterator().next();
            assertThat(action.getResult().getNumberOfAnnotations()).isEqualTo(warningsPerAxis.get(currentAxis));
        }
        AggregatedWarningsResultAction action = build.getAction(AggregatedWarningsResultAction.class);

        assertThat(action).isNotNull();
        assertThat(action.getResult().getNumberOfAnnotations()).isEqualTo(12);
    }

    private String copyResource(final String fileName) {
        Resource resource = createResource(fileName);
        try (InputStream in = resource.asInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            copy(in, out);

            // fileName can include path portion like foo/bar/zot
            return String.format(
                    "(mkdir -p %1$s || true) && rm -r %1$s && base64 --decode << ENDOFFILE | gunzip > %1$s \n%2$s\nENDOFFILE",
                    resource.getName(), new String(Base64.encodeBase64Chunked(out.toByteArray())));
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void copy(final InputStream in, final ByteArrayOutputStream out) throws IOException {
        try (OutputStream gz = new GZIPOutputStream(out)) {
            IOUtils.copy(in, gz);
        }
    }

    private Resource createResource(final String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new AssertionError("No such resource " + path + " for " + getClass().getName());
        }
        return new Resource(resource);
    }

    private WarningsPublisher enableWarnings(final MatrixProject job) {
        WarningsPublisher publisher = new WarningsPublisher();
        publisher.setConsoleParsers(new ConsoleParser[]{new ConsoleParser("GNU C Compiler 4 (gcc)")});
        job.getPublishersList().add(publisher);
        return publisher;
    }
}
