package io.jenkins.plugins.analysis.core.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.Optional;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

/**
 * Handles references to issues in the console log.
 *
 * @author Ullrich Hafner
 */
public final class ConsoleLogHandler {
    /** ID that indicates that an issue references a line in the console log of a build. */
    public static final String JENKINS_CONSOLE_LOG_FILE_NAME_ID = "jenkins-console.log";

    private static final String BRANCH_CONSOLE_LOG_PREFIX = JENKINS_CONSOLE_LOG_FILE_NAME_ID + ":";

    /**
     * Returns whether the specified issue refers to a line in the console log (either the global log or a branch log).
     *
     * @param fileName
     *         the affected file
     *
     * @return {@code true} if the issue refers to a line in the console log, {@code false} if the issue refers to a
     *         source code file in the workspace
     */
    public static boolean isInConsoleLog(final String fileName) {
        return JENKINS_CONSOLE_LOG_FILE_NAME_ID.equals(fileName) || isBranchConsoleLog(fileName);
    }

    /**
     * Returns whether the specified issue refers to a line in a branch-specific console log.
     *
     * @param fileName
     *         the affected file
     *
     * @return {@code true} if the issue refers to a line in a branch-specific console log
     */
    public static boolean isBranchConsoleLog(final String fileName) {
        return fileName != null && fileName.startsWith(BRANCH_CONSOLE_LOG_PREFIX);
    }

    /**
     * Encodes a branch identifier into a console log file name.
     *
     * @param branchIdentifier
     *         the branch identifier
     *
     * @return the encoded file name
     */
    public static String encodeFileName(final String branchIdentifier) {
        return BRANCH_CONSOLE_LOG_PREFIX + branchIdentifier;
    }

    /**
     * Extracts the branch identifier from a branch-specific console log file name.
     *
     * @param fileName
     *         the file name
     *
     * @return an {@link java.util.Optional} containing the branch identifier, or empty if not a branch log
     */
    public static Optional<String> extractBranchId(final String fileName) {
        if (isBranchConsoleLog(fileName)) {
            return Optional.of(fileName.substring(BRANCH_CONSOLE_LOG_PREFIX.length()));
        }
        return Optional.empty();
    }

    /**
     * Finds the enclosing parallel branch start node for the specified flow node.
     *
     * @param node
     *         the flow node
     * @return the enclosing branch start node, or {@code null} if not within a parallel branch
     */
    @CheckForNull
    public static BlockStartNode findEnclosingBranch(final FlowNode node) {
        if (node instanceof BlockStartNode blockStart && blockStart.getAction(ThreadNameAction.class) != null) {
            return blockStart;
        }
        for (var block : node.getEnclosingBlocks()) {
            if (block.getAction(ThreadNameAction.class) != null) {
                return block;
            }
        }
        return null;
    }

    /**
     * Compares two flow nodes by numerical ID order.
     *
     * @param n1
     *         first flow node
     * @param n2
     *         second flow node
     * @return comparison result
     */
    public static int compareNodeIds(final FlowNode n1, final FlowNode n2) {
        try {
            return Integer.compare(Integer.parseInt(n1.getId()), Integer.parseInt(n2.getId()));
        } catch (NumberFormatException e) {
            return n1.getId().compareTo(n2.getId());
        }
    }

    private ConsoleLogHandler() {
        // prevents instantiation
    }
}
