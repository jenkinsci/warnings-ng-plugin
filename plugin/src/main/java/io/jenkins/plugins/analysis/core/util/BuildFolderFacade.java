package io.jenkins.plugins.analysis.core.util;

import com.google.errorprone.annotations.MustBeClosed;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.ReaderFactory;
import hudson.model.Run;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

/**
 * Facade to the files in the build folder of the Jenkins controller. Encapsulates all calls to the running Jenkins
 * server so that tests can replace this facade with a stub.
 *
 * @author Ullrich Hafner
 */
public class BuildFolderFacade implements Serializable {
    @Serial
    private static final long serialVersionUID = 1904631270145841113L;

    /**
     * Returns the lines of the console log. If the log cannot be read, then the exception message is returned as text.
     *
     * @param build
     *         the build to get the console log for
     *
     * @return the lines of the console log
     */
    @MustBeClosed
    public Stream<String> readConsoleLog(final Run<?, ?> build) {
        return new ConsoleLogReaderFactory(build).readStream();
    }

    /**
     * Returns the lines of the console log specified by the file name. If the file name references a branch log,
     * the branch log is reconstructed from the pipeline run. If the branch log cannot be reconstructed (e.g.,
     * the pipeline execution is pruned or the node is missing), an empty {@link Optional} is returned.
     *
     * @param build
     *         the build to get the console log for
     * @param fileName
     *         the file name of the console log (either the global console log or an encoded branch console log)
     *
     * @return an {@link Optional} containing the lines of the console log, or empty if the branch log is unavailable
     */
    @MustBeClosed
    public Optional<Stream<String>> readConsoleLog(final Run<?, ?> build, final String fileName) {
        if (ConsoleLogHandler.isBranchConsoleLog(fileName)) {
            return createBranchLogReaderFactory(build, fileName).map(ReaderFactory::readStream);
        }
        return Optional.of(readConsoleLog(build));
    }

    private Optional<ReaderFactory> createBranchLogReaderFactory(final Run<?, ?> build, final String fileName) {
        var branchId = ConsoleLogHandler.extractBranchId(fileName);
        if (branchId.isEmpty() || !(build instanceof WorkflowRun workflowRun)) {
            return Optional.empty();
        }
        var execution = workflowRun.getExecution();
        if (execution == null) {
            return Optional.empty();
        }
        return findFlowNode(execution, branchId.get()).map(node -> new ConsoleLogReaderFactory(build, node));
    }

    private Optional<FlowNode> findFlowNode(final FlowExecution execution, final String id) {
        String expectedBranchId = StringUtils.substringBefore(id, ":");
        String stepId = id.contains(":") ? StringUtils.substringAfter(id, ":") : id;
        try {
            var node = execution.getNode(stepId);
            if (node == null) {
                return Optional.empty();
            }
            var branch = ConsoleLogHandler.findEnclosingBranch(node);
            if (branch == null || !expectedBranchId.equals(branch.getId())) {
                return Optional.empty();
            }
            return Optional.of(node);
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Returns the affected file with the specified file name.
     *
     * @param build
     *         the build to read the files from
     * @param fileName
     *         the file to read
     * @param sourceEncoding
     *         the encoding to use when reading the file
     *
     * @return the affected file
     * @throws IOException
     *         if the file could not be read
     */
    @MustBeClosed
    public Reader readFile(final Run<?, ?> build, final String fileName, final Charset sourceEncoding)
            throws IOException {
        InputStream inputStream = AffectedFilesResolver.asStream(build, fileName);

        return new InputStreamReader(inputStream, sourceEncoding);
    }

    /**
     * Returns whether the affected file of the specified issue can be accessed.
     *
     * @param build
     *         the build to read the files from
     * @param issue
     *         the issue to check the affected file for
     *
     * @return {@code true} if the affected file of the specified issue can be accessed, {@code false} otherwise
     */
    public boolean canAccessAffectedFileOf(final Run<?, ?> build, final Issue issue) {
        return AffectedFilesResolver.hasAffectedFile(build, issue);
    }
}
