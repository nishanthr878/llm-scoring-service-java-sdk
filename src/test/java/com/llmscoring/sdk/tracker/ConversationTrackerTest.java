package com.llmscoring.sdk.tracker;

import com.llmscoring.sdk.LLMScoring;
import com.llmscoring.sdk.config.LLMScoringConfig;
import com.llmscoring.sdk.model.TrackingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationTrackerTest {

    private LLMScoringConfig slidingWindowConfig;
    private LLMScoringConfig turnByTurnConfig;
    private LLMScoringConfig fullConversationConfig;

    @BeforeEach
    void setUp() {
        slidingWindowConfig = LLMScoringConfig.builder()
                .scoringUrl("http://localhost:8080")
                .defaultScenario("return-agent")
                .mode(TrackingMode.SLIDING_WINDOW)
                .windowSize(2)
                .silentOnError(true)
                .build();

        turnByTurnConfig = LLMScoringConfig.builder()
                .scoringUrl("http://localhost:8080")
                .defaultScenario("return-agent")
                .mode(TrackingMode.TURN_BY_TURN)
                .silentOnError(true)
                .build();

        fullConversationConfig = LLMScoringConfig.builder()
                .scoringUrl("http://localhost:8080")
                .defaultScenario("return-agent")
                .mode(TrackingMode.FULL_CONVERSATION)
                .silentOnError(true)
                .build();
    }

    @Test
    void config_shouldHaveCorrectDefaults() {
        LLMScoringConfig config = LLMScoringConfig.builder()
                .scoringUrl("http://localhost:8080")
                .build();

        assertThat(config.getMode()).isEqualTo(TrackingMode.SLIDING_WINDOW);
        assertThat(config.getWindowSize()).isEqualTo(5);
        assertThat(config.getTimeoutSeconds()).isEqualTo(10);
        assertThat(config.getMaxRetries()).isEqualTo(3);
        assertThat(config.isSilentOnError()).isTrue();
    }

    @Test
    void session_shouldCreateTrackerWithCorrectSessionId() {
        LLMScoring scoring = LLMScoring.create(slidingWindowConfig);
        ConversationTracker tracker = scoring.session("session-123");
        assertThat(tracker).isNotNull();
    }

    @Test
    void session_shouldCreateTrackerWithScenarioOverride() {
        LLMScoring scoring = LLMScoring.create(slidingWindowConfig);
        ConversationTracker tracker = scoring.session(
                "session-123", "support-agent");
        assertThat(tracker).isNotNull();
    }

    @Test
    void session_shouldCreateTrackerWithAllOptions() {
        LLMScoring scoring = LLMScoring.create(slidingWindowConfig);
        ConversationTracker tracker = scoring.session(
                "session-123", "return-agent", "gpt-4");
        assertThat(tracker).isNotNull();
    }

    @Test
    void create_shouldBuildWithBuilderPattern() {
        LLMScoring scoring = LLMScoring.create(
                LLMScoringConfig.builder()
                        .scoringUrl("http://localhost:8080")
                        .defaultScenario("return-agent")
                        .mode(TrackingMode.SLIDING_WINDOW)
                        .windowSize(3)
                        .maxRetries(2)
                        .silentOnError(true)
                        .build()
        );

        assertThat(scoring).isNotNull();
    }
}
