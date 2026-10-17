package io.jenkins.plugins.analysis.core.util;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.ParsingException;
import hudson.console.AnnotatedLargeText;
import hudson.model.Run;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.actions.LogAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link ConsoleLogReaderFactory}.
 */
class ConsoleLogReaderFactoryTest {
    @Test
    void shouldReportFileNameAndDefaultStateWhenNoFlowNodeProvided() throws IOException {
        var run = mock(Run.class);
        when(run.getCharset()).thenReturn(StandardCharsets.UTF_8);
        when(run.getLogReader()).thenReturn(new java.io.StringReader("global console log"));

        var factory = new ConsoleLogReaderFactory(run);

        assertThat(factory.getFileName()).isEqualTo(ConsoleLogHandler.JENKINS_CONSOLE_LOG_FILE_NAME_ID);
        assertThat(factory.isBranchLog()).isFalse();

        try (var reader = factory.create()) {
            assertThat(IOUtils.toString(reader)).isEqualTo("global console log");
        }
    }

    @Test
    void shouldWrapIOExceptionFromRunLogReaderInParsingException() throws IOException {
        var run = mock(Run.class);
        when(run.getCharset()).thenReturn(StandardCharsets.UTF_8);
        when(run.getLogReader()).thenThrow(new IOException("Disk error"));

        var factory = new ConsoleLogReaderFactory(run);

        assertThatThrownBy(factory::create).isInstanceOf(ParsingException.class).hasCauseInstanceOf(IOException.class);
    }

    @Test
    void shouldFallBackToRunLogWhenFlowNodeHasNoEnclosingBranch() throws IOException {
        var run = mock(Run.class);
        when(run.getCharset()).thenReturn(StandardCharsets.UTF_8);
        when(run.getLogReader()).thenReturn(new java.io.StringReader("non-parallel pipeline log"));

        var node = mock(FlowNode.class);
        doReturn(Collections.emptyList()).when(node).getEnclosingBlocks();

        var factory = new ConsoleLogReaderFactory(run, node);

        assertThat(factory.isBranchLog()).isFalse();
        try (var reader = factory.create()) {
            assertThat(IOUtils.toString(reader)).isEqualTo("non-parallel pipeline log");
        }
    }

    @Test
    void shouldReturnEmptyReaderWhenBranchHasNoPrecedingSteps() throws IOException {
        var run = mock(Run.class);
        when(run.getCharset()).thenReturn(StandardCharsets.UTF_8);

        var branchStart = mock(BlockStartNode.class);
        when(branchStart.getId()).thenReturn("branch-start");
        when(branchStart.getAction(ThreadNameAction.class)).thenReturn(mock(ThreadNameAction.class));

        var currentStep = mock(FlowNode.class);
        when(currentStep.getId()).thenReturn("current-step");
        doReturn(List.of(branchStart)).when(currentStep).getEnclosingBlocks();
        when(currentStep.getAllEnclosingIds()).thenReturn(List.of("branch-start"));
        when(currentStep.getParents()).thenReturn(Collections.emptyList());

        var factory = new ConsoleLogReaderFactory(run, currentStep);

        assertThat(factory.isBranchLog()).isTrue();
        try (var reader = factory.create()) {
            assertThat(IOUtils.toString(reader)).isEmpty();
        }
    }

    @Test
    void shouldExtractBranchLogsAndHandleNewlinesAndMissingLogs() throws IOException {
        var run = mock(Run.class);
        when(run.getCharset()).thenReturn(StandardCharsets.UTF_8);

        var branchStart = mock(BlockStartNode.class);
        when(branchStart.getId()).thenReturn("10");
        when(branchStart.getAction(ThreadNameAction.class)).thenReturn(mock(ThreadNameAction.class));

        var step1 = createStep("11", "10", Collections.emptyList(), "Step 1 output without newline");
        var step2 = createStep("12", "10", List.of(step1), "Step 2 output with newline\n");
        var step3 = createStepWithNullLogText("13", "10", List.of(step2));

        var currentStep = mock(FlowNode.class);
        when(currentStep.getId()).thenReturn("14");
        doReturn(List.of(branchStart)).when(currentStep).getEnclosingBlocks();
        when(currentStep.getAllEnclosingIds()).thenReturn(List.of("10"));
        var parents = new java.util.ArrayList<FlowNode>();
        parents.add(step3);
        parents.add(null);
        parents.add(step2);
        when(currentStep.getParents()).thenReturn(parents);

        var factory = new ConsoleLogReaderFactory(run, currentStep);

        assertThat(factory.isBranchLog()).isTrue();
        try (var reader = factory.create()) {
            List<String> lines = new BufferedReader(reader).lines().toList();
            assertThat(lines).containsExactly("Step 1 output without newline", "Step 2 output with newline");
        }
    }

    @Test
    void shouldSortStepsWithNonIntegerIdsUsingFallbackComparator() throws IOException {
        var run = mock(Run.class);
        when(run.getCharset()).thenReturn(StandardCharsets.UTF_8);

        var branchStart = mock(BlockStartNode.class);
        when(branchStart.getId()).thenReturn("bStart");
        when(branchStart.getAction(ThreadNameAction.class)).thenReturn(mock(ThreadNameAction.class));

        var stepAlpha = createStep("step-alpha", "bStart", Collections.emptyList(), "Alpha\n");
        var stepBeta = createStep("step-beta", "bStart", List.of(stepAlpha), "Beta\n");

        var currentStep = mock(FlowNode.class);
        when(currentStep.getId()).thenReturn("step-gamma");
        doReturn(List.of(branchStart)).when(currentStep).getEnclosingBlocks();
        when(currentStep.getAllEnclosingIds()).thenReturn(List.of("bStart"));
        when(currentStep.getParents()).thenReturn(List.of(stepBeta));

        var factory = new ConsoleLogReaderFactory(run, currentStep);

        try (var reader = factory.create()) {
            List<String> lines = new BufferedReader(reader).lines().toList();
            assertThat(lines).containsExactly("Alpha", "Beta");
        }
    }

    @Test
    void shouldWrapIOExceptionDuringStepLogWritingInParsingException() throws IOException {
        var run = mock(Run.class);
        when(run.getCharset()).thenReturn(StandardCharsets.UTF_8);

        var branchStart = mock(BlockStartNode.class);
        when(branchStart.getId()).thenReturn("bStart");
        when(branchStart.getAction(ThreadNameAction.class)).thenReturn(mock(ThreadNameAction.class));

        var step = mock(FlowNode.class);
        when(step.getId()).thenReturn("step-1");
        when(step.getAllEnclosingIds()).thenReturn(List.of("bStart"));
        when(step.getParents()).thenReturn(Collections.emptyList());
        var logAction = mock(LogAction.class);
        @SuppressWarnings("rawtypes")
        AnnotatedLargeText logText = mock(AnnotatedLargeText.class);
        when(logAction.getLogText()).thenReturn(logText);
        when(step.getAction(LogAction.class)).thenReturn(logAction);
        when(logText.writeLogTo(eq(0L), any(OutputStream.class))).thenThrow(new IOException("Stream error"));

        var currentStep = mock(FlowNode.class);
        when(currentStep.getId()).thenReturn("step-current");
        doReturn(List.of(branchStart)).when(currentStep).getEnclosingBlocks();
        when(currentStep.getAllEnclosingIds()).thenReturn(List.of("bStart"));
        when(currentStep.getParents()).thenReturn(List.of(step));

        var factory = new ConsoleLogReaderFactory(run, currentStep);

        assertThatThrownBy(factory::create).isInstanceOf(ParsingException.class).hasCauseInstanceOf(IOException.class);
    }

    @Test
    void shouldExcludePrecedingNodesOutsideBranch() throws IOException {
        var run = mock(Run.class);
        when(run.getCharset()).thenReturn(StandardCharsets.UTF_8);

        var branchStart = mock(BlockStartNode.class);
        when(branchStart.getId()).thenReturn("10");
        when(branchStart.getAction(ThreadNameAction.class)).thenReturn(mock(ThreadNameAction.class));

        var outsideStep = createStep("05", "pipeline-root", Collections.emptyList(), "Outside branch output\n");
        var stepInBranch = createStep("11", "10", List.of(outsideStep), "Inside branch output\n");

        var currentStep = createCurrentStep("12", branchStart, List.of(stepInBranch));

        var factory = new ConsoleLogReaderFactory(run, currentStep);

        try (var reader = factory.create()) {
            List<String> lines = new BufferedReader(reader).lines().toList();
            assertThat(lines).containsExactly("Inside branch output");
        }
    }

    @Test
    void shouldDeduplicateSharedGraphAncestorsInBranch() throws IOException {
        var run = mock(Run.class);
        when(run.getCharset()).thenReturn(StandardCharsets.UTF_8);

        var branchStart = mock(BlockStartNode.class);
        when(branchStart.getId()).thenReturn("10");
        when(branchStart.getAction(ThreadNameAction.class)).thenReturn(mock(ThreadNameAction.class));

        var sharedAncestor = createStep("11", "10", Collections.emptyList(), "Shared ancestor output\n");
        var pathA = createStep("12", "10", List.of(sharedAncestor), "Path A output\n");
        var pathB = createStep("13", "10", List.of(sharedAncestor), "Path B output\n");

        var currentStep = createCurrentStep("14", branchStart, List.of(pathA, pathB));

        var factory = new ConsoleLogReaderFactory(run, currentStep);

        try (var reader = factory.create()) {
            List<String> lines = new BufferedReader(reader).lines().toList();
            assertThat(lines).containsExactly("Shared ancestor output", "Path A output", "Path B output");
        }
    }

    private FlowNode createCurrentStep(
            final String id, final BlockStartNode branchStart, final List<FlowNode> parents) {
        var currentStep = mock(FlowNode.class);
        when(currentStep.getId()).thenReturn(id);
        doReturn(List.of(branchStart)).when(currentStep).getEnclosingBlocks();
        var branchId = branchStart.getId();
        when(currentStep.getAllEnclosingIds()).thenReturn(List.of(branchId));
        when(currentStep.getParents()).thenReturn(parents);
        return currentStep;
    }

    @SuppressWarnings("rawtypes")
    private FlowNode createStep(
            final String id, final String enclosingId, final List<FlowNode> parents, final String logOutput)
            throws IOException {
        var step = mock(FlowNode.class);
        when(step.getId()).thenReturn(id);
        when(step.getAllEnclosingIds()).thenReturn(List.of(enclosingId));
        when(step.getParents()).thenReturn(parents);
        var logAction = mock(LogAction.class);
        AnnotatedLargeText logText = mock(AnnotatedLargeText.class);
        when(logAction.getLogText()).thenReturn(logText);
        when(step.getAction(LogAction.class)).thenReturn(logAction);
        when(logText.writeLogTo(eq(0L), any(OutputStream.class))).thenAnswer(invocation -> {
            OutputStream out = invocation.getArgument(1);
            byte[] bytes = logOutput.getBytes(StandardCharsets.UTF_8);
            out.write(bytes);
            return (long) bytes.length;
        });
        return step;
    }

    private FlowNode createStepWithNullLogText(
            final String id, final String enclosingId, final List<FlowNode> parents) {
        var step = mock(FlowNode.class);
        when(step.getId()).thenReturn(id);
        when(step.getAllEnclosingIds()).thenReturn(List.of(enclosingId));
        when(step.getParents()).thenReturn(parents);
        var logAction = mock(LogAction.class);
        when(logAction.getLogText()).thenReturn(null);
        when(step.getAction(LogAction.class)).thenReturn(logAction);
        return step;
    }
}
