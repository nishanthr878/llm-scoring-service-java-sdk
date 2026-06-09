package com.llmscoring.sdk.tracker;

import com.llmscoring.sdk.LLMScoring;
import com.llmscoring.sdk.config.LLMScoringConfig;
import com.llmscoring.sdk.model.TrackingMode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shows exactly how an integrator uses the SDK.
 * No scoring service needed — just verifies the API is correct.
 */
class SDKUsageExampleTest {

    @Test
    void usage_slidingWindowMode() {
        // Step 1 — configure once at app startup
        LLMScoring scoring = LLMScoring.create(
                LLMScoringConfig.builder()
                        .scoringUrl("http://your-scoring-service")
                        .defaultScenario("return-agent")
                        .mode(TrackingMode.SLIDING_WINDOW)
                        .windowSize(5)
                        .silentOnError(true)
                        .build()
        );

        // Step 2 — create tracker per session
        ConversationTracker tracker = scoring.session(
                "session-abc-123",
                "return-agent",
                "gpt-4"
        );

        assertThat(tracker).isNotNull();

        // Step 3 — call track() after every bot response
        // This is all the integrator writes in their bot code
        // tracker.track(userMessage, botResponse);
    }

    @Test
    void usage_withCallback() {
        LLMScoring scoring = LLMScoring.create(
                LLMScoringConfig.builder()
                        .scoringUrl("http://your-scoring-service")
                        .defaultScenario("support-agent")
                        .mode(TrackingMode.FULL_CONVERSATION)
                        .silentOnError(true)
                        .build()
        );

        ConversationTracker tracker = scoring
                .session("session-xyz-456", "support-agent")
                .onComplete(result -> {
                    // Called when scoring completes
                    if (result != null && Boolean.FALSE.equals(result.getOverallPassed())) {
                        System.out.println("ALERT: Session failed scoring — "
                                + result.getFlagReasons());
                    }
                });

        assertThat(tracker).isNotNull();
    }

    @Test
    void usage_turnByTurnMode() {
        LLMScoring scoring = LLMScoring.create(
                LLMScoringConfig.builder()
                        .scoringUrl("http://your-scoring-service")
                        .defaultScenario("sales-agent")
                        .mode(TrackingMode.TURN_BY_TURN)
                        .silentOnError(true)
                        .build()
        );

        ConversationTracker tracker = scoring.session("session-789");
        assertThat(tracker).isNotNull();
    }

    @Test
    void usage_defaultConfigValues() {
        LLMScoringConfig config = LLMScoringConfig.builder()
                .scoringUrl("http://localhost:8080")
                .build();

        assertThat(config.getMode()).isEqualTo(TrackingMode.SLIDING_WINDOW);
        assertThat(config.getWindowSize()).isEqualTo(5);
        assertThat(config.getTimeoutSeconds()).isEqualTo(10);
        assertThat(config.getMaxRetries()).isEqualTo(3);
        assertThat(config.isSilentOnError()).isTrue();
    }
}
