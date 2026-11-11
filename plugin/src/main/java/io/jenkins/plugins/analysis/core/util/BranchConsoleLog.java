package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.analysis.ReaderFactory;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

/**
 * Represents the isolated console log of a parallel pipeline branch.
 *
 * @author Ullrich Hafner
 */
public class BranchConsoleLog implements ConsoleLog {
    private final FlowNode flowNode;

    /**
     * Creates a new instance of {@link BranchConsoleLog}.
     *
     * @param flowNode
     *         the flow node of the current execution within the parallel branch
     */
    public BranchConsoleLog(final FlowNode flowNode) {
        this.flowNode = flowNode;
    }

    /**
     * Returns the flow node of the execution.
     *
     * @return the flow node
     */
    public FlowNode getFlowNode() {
        return flowNode;
    }

    @Override
    public ReaderFactory createReaderFactory(final Run<?, ?> run) {
        return new ConsoleLogReaderFactory(run, flowNode);
    }

    @Override
    public boolean isBranchLog() {
        return true;
    }
}
