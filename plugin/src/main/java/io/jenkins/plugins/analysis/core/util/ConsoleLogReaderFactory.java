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
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
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

    /**
     * Returns whether this reader factory is operating on a branch-specific console log.
     *
     * @return {@code true} if scoped to a parallel branch, {@code false} if reading the global run log
     */
    public boolean isBranchLog() {
        return flowNode != null && findEnclosingBranch(flowNode) != null;
    }

    /**
     * Returns the name of the resource.
     *
     * @return the file name
     */
    @Override
    public String getFileName() {
        return ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID;
    }

    /**
     * Creates a new {@link Reader} for the file.
     *
     * @return a reader
     */
    @Override
    @MustBeClosed
    public Reader create() {
        if (flowNode != null) {
            var branchLog = getBranchLogReader(flowNode);
            if (branchLog.isPresent()) {
                return branchLog.get();
            }
        }
        try {
            return run.getLogReader();
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    private Optional<Reader> getBranchLogReader(final FlowNode node) {
        var branchStart = findEnclosingBranch(node);
        if (branchStart == null) {
            return Optional.empty();
        }

        var stepsInBranch = collectPrecedingStepsInBranch(node, branchStart);
        if (stepsInBranch.isEmpty()) {
            return Optional.of(new StringReader(StringUtils.EMPTY));
        }

        var branchLog = new AppendableByteArrayOutputStream();
        try {
            for (var step : stepsInBranch) {
                var logAction = step.getAction(LogAction.class);
                if (logAction != null && logAction.getLogText() != null) {
                    long bytes = logAction.getLogText().writeLogTo(0, branchLog);
                    if (bytes > 0 && !branchLog.endsWithNewline()) {
                        branchLog.write('\n');
                    }
                }
            }
            return Optional.of(
                    new InputStreamReader(new ByteArrayInputStream(branchLog.toByteArray()), run.getCharset()));
        } catch (IOException e) {
            throw new ParsingException(e);
        }
    }

    @CheckForNull
    private BlockStartNode findEnclosingBranch(final FlowNode node) {
        for (var block : node.getEnclosingBlocks()) {
            if (block.getAction(ThreadNameAction.class) != null) {
                return block;
            }
        }
        return null;
    }

    private List<FlowNode> collectPrecedingStepsInBranch(final FlowNode currentStep, final BlockStartNode branchStart) {
        var stepsInBranch = new ArrayList<FlowNode>();
        var visited = new HashSet<String>();
        var queue = new ArrayDeque<FlowNode>();

        queue.add(currentStep);
        visited.add(currentStep.getId());

        var branchStartId = branchStart.getId();
        while (!queue.isEmpty()) {
            var curr = queue.poll();
            if (!curr.equals(currentStep)
                    && curr.getAllEnclosingIds().contains(branchStartId)
                    && curr.getAction(LogAction.class) != null) {
                stepsInBranch.add(curr);
            }
            for (var parent : curr.getParents()) {
                if (parent != null && visited.add(parent.getId())) {
                    if (parent.getAllEnclosingIds().contains(branchStartId)) {
                        queue.add(parent);
                    }
                }
            }
        }

        stepsInBranch.sort((n1, n2) -> {
            try {
                return Integer.compare(Integer.parseInt(n1.getId()), Integer.parseInt(n2.getId()));
            } catch (NumberFormatException e) {
                return n1.getId().compareTo(n2.getId());
            }
        });

        return stepsInBranch;
    }

    private static class AppendableByteArrayOutputStream extends ByteArrayOutputStream {
        boolean endsWithNewline() {
            return count > 0 && buf[count - 1] == '\n';
        }
    }
}
