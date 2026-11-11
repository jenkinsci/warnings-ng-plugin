package io.jenkins.plugins.analysis.core.util;

import com.google.errorprone.annotations.MustBeClosed;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.console.ConsoleNote;
import hudson.model.Run;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

/**
 * Provides a reader factory for Jenkins' console log. If a {@link FlowNode} is provided and is part of a parallel
 * pipeline branch, this reader factory extracts and concatenates only the console logs from the steps within that
 * branch, avoiding cross-talk and interleaved output from concurrent branches.
 *
 * @author Ullrich Hafner
 */
public class ConsoleLogReaderFactory extends ReaderFactory {
    private final Run<?, ?> run;

    @CheckForNull
    private final FlowNode flowNode;

    private static final Pattern TIME_STAMPER_PREFIX =
            Pattern.compile("\\[\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z] ");
    private static final Function<String, String> REMOVE_TIME_STAMPER_PREFIX =
            string -> TIME_STAMPER_PREFIX.matcher(string).replaceFirst(StringUtils.EMPTY);

    /**
     * Creates a new {@link ConsoleLogReaderFactory}.
     *
     * @param run
     *         the run that provides the console log
     */
    public ConsoleLogReaderFactory(final Run<?, ?> run) {
        this(run, null);
    }

    /**
     * Creates a new {@link ConsoleLogReaderFactory} with an optional pipeline {@link FlowNode} context.
     *
     * @param run
     *         the run that provides the console log
     * @param flowNode
     *         the flow node of the current step execution (or {@code null} if outside pipeline or unknown)
     */
    public ConsoleLogReaderFactory(final Run<?, ?> run, @CheckForNull final FlowNode flowNode) {
        super(run.getCharset(), REMOVE_TIME_STAMPER_PREFIX.compose(ConsoleNote::removeNotes));

        this.run = run;
        this.flowNode = flowNode;
    }

    private final ReentrantLock lock = new ReentrantLock();
    private boolean branchEvaluated;

    @CheckForNull
    private BlockStartNode enclosingBranch;

    private byte[] branchLogContent;

    /**
     * Returns whether this reader factory is operating on a branch-specific console log.
     *
     * @return {@code true} if scoped to a parallel branch, {@code false} if reading the global run log
     */
    public boolean isBranchLog() {
        return getEnclosingBranch() != null;
    }

    /**
     * Returns the name of the resource.
     *
     * @return the file name
     */
    @Override
    public String getFileName() {
        var branch = getEnclosingBranch();
        if (branch != null && flowNode != null) {
            String branchId = flowNode.equals(branch) ? branch.getId() : branch.getId() + ":" + flowNode.getId();
            return ConsoleLogHandler.encodeFileName(branchId);
        }
        return ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID;
    }

    private BlockStartNode getEnclosingBranch() {
        lock.lock();
        try {
            if (!branchEvaluated) {
                if (flowNode != null) {
                    enclosingBranch = ConsoleLogHandler.findEnclosingBranch(flowNode);
                }
                branchEvaluated = true;
            }
            return enclosingBranch;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates a new {@link Reader} for the file.
     *
     * @return a reader
     */
    @Override
    @MustBeClosed
    public Reader create() {
        var branch = getEnclosingBranch();
        if (branch != null && flowNode != null) {
            return getBranchLogReader(flowNode, branch);
        }
        try {
            return run.getLogReader();
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    private Reader getBranchLogReader(final FlowNode node, final BlockStartNode branchStart) {
        lock.lock();
        try {
            if (branchLogContent == null) {
                var stepsInBranch = collectPrecedingStepsInBranch(node, branchStart);
                if (stepsInBranch.isEmpty()) {
                    branchLogContent = new byte[0];
                } else {
                    try (var branchLog = new AppendableByteArrayOutputStream()) {
                        for (var step : stepsInBranch) {
                            appendStepLog(branchLog, step);
                        }
                        branchLogContent = branchLog.toByteArray();
                    } catch (IOException e) {
                        throw new ParsingException(e);
                    }
                }
            }
            return new InputStreamReader(new ByteArrayInputStream(branchLogContent), run.getCharset());
        } finally {
            lock.unlock();
        }
    }

    private void appendStepLog(final AppendableByteArrayOutputStream branchLog, final FlowNode step)
            throws IOException {
        var logAction = step.getAction(LogAction.class);
        if (logAction != null && logAction.getLogText() != null) {
            long bytes = logAction.getLogText().writeLogTo(0, branchLog);
            if (bytes > 0 && !branchLog.endsWithNewline()) {
                branchLog.write('\n');
            }
        }
    }

    private List<FlowNode> collectPrecedingStepsInBranch(final FlowNode currentStep, final BlockStartNode branchStart) {
        Collection<FlowNode> startNodes = getTraversalRoots(currentStep, branchStart);
        if (startNodes.isEmpty()) {
            return List.of();
        }

        var branchStartId = branchStart.getId();
        var stepsInBranch = new ArrayList<FlowNode>();
        var visited = new HashSet<String>();
        var queue = new ArrayDeque<>(startNodes);

        for (FlowNode root : startNodes) {
            visited.add(root.getId());
        }

        boolean isBranchReconstruction = currentStep.equals(branchStart);
        while (!queue.isEmpty()) {
            var curr = queue.poll();
            if ((isBranchReconstruction || !curr.equals(currentStep))
                    && curr.getAllEnclosingIds().contains(branchStartId)
                    && curr.getAction(LogAction.class) != null) {
                stepsInBranch.add(curr);
            }
            addUnvisitedParents(curr, branchStartId, visited, queue);
        }

        stepsInBranch.sort(ConsoleLogHandler::compareNodeIds);
        return stepsInBranch;
    }

    private Collection<FlowNode> getTraversalRoots(final FlowNode currentStep, final BlockStartNode branchStart) {
        if (currentStep.equals(branchStart)) {
            var execution = currentStep.getExecution();
            if (execution != null) {
                return execution.getCurrentHeads();
            }
            return List.of();
        }
        return List.of(currentStep);
    }

    private void addUnvisitedParents(
            final FlowNode node, final String branchStartId, final Set<String> visited, final Queue<FlowNode> queue) {
        for (var parent : node.getParents()) {
            if (parent != null
                    && visited.add(parent.getId())
                    && parent.getAllEnclosingIds().contains(branchStartId)) {
                queue.add(parent);
            }
        }
    }

    private static class AppendableByteArrayOutputStream extends ByteArrayOutputStream {
        boolean endsWithNewline() {
            return count > 0 && buf[count - 1] == '\n';
        }
    }
}
