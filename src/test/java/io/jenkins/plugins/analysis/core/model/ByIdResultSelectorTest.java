package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;

/**
 * Tests the class {@link ByIdResultSelector}.
 *
 * @author Alexander Praegla
 */
class ByIdResultSelectorTest {
    /**
     * Verifies the returned optional is empty if the provided list of {@link ResultAction} is empty.
     */
    @Test
    void shouldBeEmptyIfActionsAreEmpty() {
        ByIdResultSelector selector = new ByIdResultSelector("1");

        Run<?, ?> run = createRunStub(new ArrayList<>());

        assertThat(selector.get(run)).isEmpty();
    }

    /**
     * Verifies the returned optional is empty if the provided list of {@link ResultAction} does not contains the id of
     * the instance of {@link ByIdResultSelector}.
     */
    @Test
    void shouldBeEmptyIfActionsHasDifferentId() {
        ByIdResultSelector selector = new ByIdResultSelector("1");

        List<ResultAction> resultActions = new ArrayList<>();
        resultActions.add(createResultActionStub("2"));

        Run<?, ?> run = createRunStub(resultActions);

        assertThat(selector.get(run)).isEmpty();
    }

    /**
     * Verifies the returned optional is not empty if the provided list of {@link ResultAction} contains one element.
     */
    @Test
    void shouldFindActionWithSameId() {
        ByIdResultSelector selector = new ByIdResultSelector("1");

        List<ResultAction> resultActions = new ArrayList<>();
        resultActions.add(createResultActionStub("1"));

        Run<?, ?> run = createRunStub(resultActions);

        assertThat(selector.get(run)).isPresent();
    }

    /**
     * Verifies the returned optional is empty if the provided list of {@link ResultAction} contains two elements with
     * different id.
     */
    @Test
    void shouldFindEmptyIfListContainsTwoDifferentIds() {
        ByIdResultSelector selector = new ByIdResultSelector("1");

        List<ResultAction> resultActions = new ArrayList<>();
        resultActions.add(createResultActionStub("2"));
        resultActions.add(createResultActionStub("3"));

        Run<?, ?> run = createRunStub(resultActions);

        assertThat(selector.get(run)).isEmpty();
    }

    /**
     * Verifies the returned optional is not empty if the provided list of {@link ResultAction} contains two elements.
     */
    @Test
    void shouldFindActionWithSameIdIfListContainsTwoDifferentIds() {
        ByIdResultSelector selector = new ByIdResultSelector("1");

        List<ResultAction> resultActions = new ArrayList<>();
        resultActions.add(createResultActionStub("2"));
        resultActions.add(createResultActionStub("1"));

        Run<?, ?> run = createRunStub(resultActions);

        assertThat(selector.get(run)).isPresent();
    }

    @Test
    void testToString() {
        ByIdResultSelector selector = new ByIdResultSelector("1");
        assertThat(selector.toString()).isEqualTo("io.jenkins.plugins.analysis.core.model.ResultAction with ID 1");
    }

    private Run<?, ?> createRunStub(final List<ResultAction> resultActions) {
        Run<?, ?> run = mock(Run.class);
        when(run.getActions(ResultAction.class)).thenReturn(resultActions);
        return run;
    }

    private ResultAction createResultActionStub(final String mockedIdValue) {
        ResultAction resultAction = mock(ResultAction.class);
        when(resultAction.getId()).thenReturn(mockedIdValue);
        return resultAction;
    }
}