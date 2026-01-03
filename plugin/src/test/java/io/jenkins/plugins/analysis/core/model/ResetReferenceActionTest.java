package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link ResetReferenceAction}.
 *
 * @author Akash Manna
 */
class ResetReferenceActionTest {
    private static final String TOOL_ID = "test-tool";
    private static final String USER_ID = "testuser";
    private static final String REASON = "Testing quality gate reset";

    @Test
    void shouldStoreToolId() {
        ResetReferenceAction action = new ResetReferenceAction(TOOL_ID);

        assertThat(action.getId()).isEqualTo(TOOL_ID);
    }

    @Test
    void shouldCaptureCurrentUserAndTimestamp() {
        long beforeTimestamp = System.currentTimeMillis();
        ResetReferenceAction action = new ResetReferenceAction(TOOL_ID);
        long afterTimestamp = System.currentTimeMillis();

        assertThat(action.getUserId()).isNotNull();
        assertThat(action.getTimestamp()).isBetween(beforeTimestamp, afterTimestamp);
        assertThat(action.getFormattedTimestamp()).isNotEmpty();
    }

    @Test
    void shouldStoreUserAndTimestampExplicitly() {
        long timestamp = 1_609_459_200_000L; // 2021-01-01 00:00:00 GMT

        ResetReferenceAction action = new ResetReferenceAction(TOOL_ID, USER_ID, timestamp);

        assertThat(action.getId()).isEqualTo(TOOL_ID);
        assertThat(action.getUserId()).isEqualTo(USER_ID);
        assertThat(action.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldProvideUserName() {
        ResetReferenceAction action = new ResetReferenceAction(TOOL_ID, USER_ID, System.currentTimeMillis());

        assertThat(action.getUserName()).isEqualTo(USER_ID);
    }

    @Test
    void shouldFormatTimestampCorrectly() {
        long timestamp = 1_609_459_200_000L; // 2021-01-01 00:00:00 GMT
        ResetReferenceAction action = new ResetReferenceAction(TOOL_ID, USER_ID, timestamp);

        String formattedTimestamp = action.getFormattedTimestamp();
        assertThat(formattedTimestamp).isNotEmpty();
    }

    @Test
    void shouldReturnNullForActionMethods() {
        ResetReferenceAction action = new ResetReferenceAction(TOOL_ID);

        assertThat(action.getIconFileName()).isNull();
        assertThat(action.getDisplayName()).isNull();
        assertThat(action.getUrlName()).isNull();
    }
}
