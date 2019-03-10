package io.jenkins.plugins.analysis.warnings.recorder;

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

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import static hudson.Functions.*;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Gcc4;
import io.jenkins.plugins.analysis.warnings.Resource;

import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.matrix.TextAxis;
import hudson.model.Result;
import hudson.tasks.Shell;

/**
 * Integration tests of the warnings plug-in in matrix jobs.
 *
 * @author Ullrich Hafner
 */
public class MatrixJobITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String WARNINGS_FILE = "matrix-warnings.txt";

    /**
     * Build a matrix job with three configurations. For each configuration a different set of warnings will be parsed
     * with the same parser (GCC). After the successful build the total number of warnings at the root level should be
     * set to 12 (sum of all three configurations). Moreover, for each configuration the total number of warnings is
     * also verified (4, 6, and 2 warnings).
     *
     * @throws Exception in case of an error
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    public void shouldCreateIndividualAxisResults() throws Exception {
        MatrixProject project = createProject(MatrixProject.class);
        copySingleFileToWorkspace(project, "matrix-warnings-one.txt", "user_axis/one/warnings.txt");
        copySingleFileToWorkspace(project, "matrix-warnings-two.txt", "user_axis/two/warnings.txt");
        copySingleFileToWorkspace(project, "matrix-warnings-three.txt", "user_axis/three/warnings.txt");
        IssuesRecorder publisher = new IssuesRecorder();
        Gcc4 tool = new Gcc4();
        tool.setPattern("**/*.txt");
        publisher.setTool(tool);
        project.getPublishersList().add(publisher);

        AxisList axis = new AxisList();
        TextAxis userAxis = new TextAxis("user_axis", "one two three");
        axis.add(userAxis);
        project.setAxes(axis);

        Map<String, Integer> warningsPerAxis = new HashMap<>();
        warningsPerAxis.put("one", 4);
        warningsPerAxis.put("two", 6);
        warningsPerAxis.put("three", 2);

        MatrixBuild build = project.scheduleBuild2(0).get();
        for (MatrixRun run : build.getRuns()) {
            getJenkins().assertBuildStatus(Result.SUCCESS, run);

            AnalysisResult result = getAnalysisResult(run);

            String currentAxis = run.getBuildVariables().values().iterator().next();
            assertThat(result.getTotalSize()).as("Result of axis " + currentAxis).isEqualTo(warningsPerAxis.get(currentAxis));
        }
        AnalysisResult aggregation = getAnalysisResult(build);
        assertThat(aggregation.getTotalSize()).isEqualTo(12);
    }

    private String copyResource(final String fileName) {
        Resource resource = createResource(fileName);
        try (InputStream in = resource.asInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            copy(in, out);

            // fileName can include path portion like foo/bar/zot
            return String.format(
                    "(mkdir -p %1$s || true) && rm -r %1$s && base64 --decode << ENDOFFILE | gunzip > %1$s %n%2$s%nENDOFFILE",
                    resource.getName(), new String(Base64.encodeBase64Chunked(out.toByteArray())));
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void copy(final InputStream input, final ByteArrayOutputStream output) throws IOException {
        try (OutputStream gz = new GZIPOutputStream(output)) {
            IOUtils.copy(input, gz);
        }
    }

    private Resource createResource(final String path) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new AssertionError("No such resource " + path + " for " + getClass().getName());
        }
        return new Resource(resource);
    }
}
