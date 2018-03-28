package io.jenkins.plugins.analysis.core.model;

import hudson.model.Run;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ByIdResultSelectorTest {

	private ByIdResultSelector cut;

	@Test
	void shouldBeEmptyOptionalFromEmptyList() {
		cut  = new ByIdResultSelector("1");
		Run<?, ?> run = mock(Run.class);
		when(run.getActions(ResultAction.class)).thenReturn(new ArrayList<>());

		Optional<ResultAction> resultAction = cut.get(run);
		assertThat(resultAction).isEmpty();
	}

	@Test
	void shouldBeEmptyOptionalFromList() {
		cut  = new ByIdResultSelector("1");
		ResultAction resultAction = mock(ResultAction.class);
		when(resultAction.getId()).thenReturn("2");

		List<ResultAction> resultActions = new ArrayList<>();
		resultActions.add(resultAction);

		Run<?, ?> run = mock(Run.class);
		when(run.getActions(ResultAction.class)).thenReturn(resultActions);

		assertThat(cut.get(run)).isEmpty();
	}

	@Test
	void shouldBeNonEmptyOptional() {
		cut  = new ByIdResultSelector("1");
		ResultAction resultAction = mock(ResultAction.class);
		when(resultAction.getId()).thenReturn("1");

		List<ResultAction> resultActions = new ArrayList<>();
		resultActions.add(resultAction);

		Run<?, ?> run = mock(Run.class);
		when(run.getActions(ResultAction.class)).thenReturn(resultActions);

		assertThat(cut.get(run)).isPresent();
	}

	@Test
	void testToString() {
		cut  = new ByIdResultSelector("1");
		assertThat(cut.toString()).isEqualTo("io.jenkins.plugins.analysis.core.views.ResultAction with ID 1");
	}
}