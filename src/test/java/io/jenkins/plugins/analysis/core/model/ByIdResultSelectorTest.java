package io.jenkins.plugins.analysis.core.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Run;

/**
 * Unit test for {@link ByIdResultSelector}.
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
class ByIdResultSelectorTest {

    private static final int NO_RESULT_EXPECTED = -1;
    private static final String ID_NAME_1 = "IdName";
    private static final String ID_NAME_2 = "IdName2";
    private static final String ID_NAME_3 = "IdName3";
    private static final String ID_NAME_4 = "IdName4";

    /**
     * Verifies the correct return value for an empty result list where the object has been created with a non null value.
     */
    @Test
    void shouldReturnNothingForEmptyResultActionList() {
        applyGetMethodAndCheckResult(ID_NAME_1, NO_RESULT_EXPECTED);
    }

    /**
     * Verifies the correct return value for an empty result list where the object has been created with a null value.
     */
    @Test
    void shouldReturnNothingForEmptyResultActionListWhenTheIdOfTheConstructedObjectIsNull() {
        applyGetMethodAndCheckResult(null, NO_RESULT_EXPECTED);
    }

    /**
     * Verifies the correct return value for a result list where the searched object is contained.
     */
    @Test
    void shouldReturnTheExpectedIdNameForAnOneElementListContainingTheExpectedId() {
        applyGetMethodAndCheckResult(ID_NAME_1, 0, ID_NAME_1);
    }

    /**
     * Verifies the correct return value for a result list where the searched object is not contained.
     */
    @Test
    void shouldReturnNothingForAnOneElementListNotContainingTheExpectedId() {
        applyGetMethodAndCheckResult(ID_NAME_1, NO_RESULT_EXPECTED, ID_NAME_2);
        applyGetMethodAndCheckResult(ID_NAME_2, NO_RESULT_EXPECTED, ID_NAME_1);
    }

    /**
     * Verifies the correct return value for a result list where the searched object is contained.
     */
    @Test
    void shouldReturnTheExpectedIdNameForAListContainingTheExpectedId() {
        applyGetMethodAndCheckResult(ID_NAME_1, 0, ID_NAME_1, ID_NAME_2, ID_NAME_3);
        applyGetMethodAndCheckResult(ID_NAME_1, 1, ID_NAME_2, ID_NAME_1, ID_NAME_3);
        applyGetMethodAndCheckResult(ID_NAME_1, 2, ID_NAME_3, ID_NAME_2, ID_NAME_1);

    }

    /**
     * Verifies the correct return value for a result list where the searched object is not contained.
     */
    @Test
    void shouldReturnTheExpectedIdNameForAListNotContainingTheExpectedId() {
        applyGetMethodAndCheckResult(ID_NAME_4, NO_RESULT_EXPECTED, ID_NAME_1, ID_NAME_2, ID_NAME_3);
        applyGetMethodAndCheckResult(ID_NAME_4, NO_RESULT_EXPECTED, ID_NAME_2, ID_NAME_1, ID_NAME_3);
        applyGetMethodAndCheckResult(ID_NAME_4, NO_RESULT_EXPECTED, ID_NAME_3, ID_NAME_2, ID_NAME_1);
    }

    /**
     * Verifies that an equals call on a null object is resulting in the expected output.
     */
    @Test
    void shouldReturnNothingForAnEqualsCallOnANullObject() {
        final ByIdResultSelector byIdResultSelector = new ByIdResultSelector(ID_NAME_1);
        final Run run = mock(Run.class);
        final List<ResultAction> actionArrayList = new LinkedList<>();
        final ResultAction resultAction = mock(ResultAction.class);
        when(run.getActions(ResultAction.class)).thenReturn(actionArrayList);
        when(resultAction.getId()).thenReturn(null);

        assertThat(byIdResultSelector.get(run)).isEqualTo(Optional.empty());
    }

    /**
     * Verifies that a null pointer exception is thrown when an equals call on a null object happens.
     */
    @Test
    void shouldThrowNullPointerExceptionWhenAnEqualsCallOnANullObjectHappens() {
        final ByIdResultSelector byIdResultSelector = new ByIdResultSelector(null);
        final Run run = mock(Run.class);
        final List<ResultAction> actionArrayList = new LinkedList<>();
        final ResultAction resultAction = mock(ResultAction.class);
        actionArrayList.add(resultAction);
        when(run.getActions(ResultAction.class)).thenReturn(actionArrayList);

        assertThatNullPointerException().isThrownBy(
                () -> byIdResultSelector.get(run))
                .withNoCause();
    }

    /**
     * Verifies that a null pointer exception is thrown when the run object is null.
     */
    @Test
    void shouldThrowNullPointerExceptionForARunObjectWhichIsNull() {
        final ByIdResultSelector byIdResultSelector = new ByIdResultSelector("Anything");

        assertThatNullPointerException().isThrownBy(
                () -> byIdResultSelector.get(null))
                .withNoCause();
    }

    /**
     * Verifies the correct string representation for a null value based on an hardcoded classname.
     */
    @Test
    void shouldReturnCorrectStringRepresentationForAGivenIdBasedOnAHardcodedClassname() {
        final ByIdResultSelector byIdResultSelector = new ByIdResultSelector("ID-Name");

        assertThat(byIdResultSelector.toString()).isEqualTo(
                "io.jenkins.plugins.analysis.core.views.ResultAction with ID ID-Name");
    }

    /**
     * Verifies the correct string representation for a value based on the actual classname.
     */
    @Test
    void shouldReturnCorrectStringRepresentationForAGivenIdBasedOnTheActualClassname() {
        ByIdResultSelector byIdResultSelector = new ByIdResultSelector("ID-Name");

        assertThat(byIdResultSelector.toString()).isEqualTo(
                String.format("%s with ID ID-Name", ResultAction.class.getName()));
    }

    /**
     * Verifies the correct string representation for a null value based on the actual classname.
     */
    @Test
    void shouldReturnCorrectStringRepresentationForAnIdWhichIsNullBasedOnTheActualClassname() {
        ByIdResultSelector byIdResultSelector = new ByIdResultSelector(null);

        assertThat(byIdResultSelector.toString()).isEqualTo(
                String.format("%s with ID null", ResultAction.class.getName()));
    }

    private void applyGetMethodAndCheckResult(final String constructorId, final int expectedResult,
            final String... listIds) {
        final ByIdResultSelector byIdResultSelector = new ByIdResultSelector(constructorId);
        final Run run = mock(Run.class);

        final List<ResultAction> actionArrayList = fillListWithMockedObjects(listIds);

        when(run.getActions(ResultAction.class)).thenReturn(actionArrayList);

        if (expectedResult == NO_RESULT_EXPECTED) {
            assertThat(byIdResultSelector.get(run)).isEqualTo(Optional.empty());
        }
        else {
            assertThat(byIdResultSelector.get(run)).isEqualTo(Optional.of(actionArrayList.get(expectedResult)));
        }
    }

    private List<ResultAction> fillListWithMockedObjects(final String... idNames) {
        final List<ResultAction> actionArrayList = new LinkedList<>();

        for (String idName : idNames) {
            final ResultAction resultAction = mock(ResultAction.class);
            when(resultAction.getId()).thenReturn(idName);
            actionArrayList.add(resultAction);
        }
        return actionArrayList;
    }

}
