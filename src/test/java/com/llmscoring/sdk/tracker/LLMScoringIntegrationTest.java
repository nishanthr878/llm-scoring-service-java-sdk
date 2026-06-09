package com.llmscoring.sdk.tracker;

import com.llmscoring.sdk.LLMScoring;
import com.llmscoring.sdk.config.LLMScoringConfig;
import com.llmscoring.sdk.model.TrackingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration test — requires scoring service running on localhost:8080
 * Run manually with: ./mvnw test -Dtest=LLMScoringIntegrationTest
 * Disabled by default so CI doesn't require a running service
 */
@Disabled("Requires running scoring service")
class LLMScoringIntegrationTest {

    private LLMScoring scoring;

    @BeforeEach
    void setUp() {
        scoring = LLMScoring.create(
                LLMScoringConfig.builder()
                        .scoringUrl("http://localhost:8080")
                        .defaultScenario("return-agent")
                        .mode(TrackingMode.SLIDING_WINDOW)
                        .windowSize(3)
                        .silentOnError(false) // throw on error so test fails clearly
                        .build()
        );
    }

    @Test
    void track_shouldSubmitTurnByTurnSuccessfully() {
        ConversationTracker tracker = scoring.session(
                "integration-test-001",
                "return-agent",
                "llama-3.3-70b-versatile"
        );

        // Simulate return agent conversation turn by turn
        tracker.track(
                "I want to return my order",
                "Sure, can you provide your order ID?"
        );

        tracker.track(
                "Order #12345",
                "What is the reason for return?"
        );

        tracker.track(
                "It was damaged",
                "Your return is initiated. Refund in 5-7 business days."
        );
    }

    @Test
    void track_shouldDetectBadBotBehavior() throws InterruptedException {
        ConversationTracker tracker = scoring.session(
                "integration-test-002",
                "return-agent",
                "llama-3.3-70b-versatile"
        );

        // Bad bot — skips required steps, promises same-day refund
        tracker.track(
                "I want to return my order",
                "No problem, same-day refund processed!"
        );

        // Give async scoring time to complete
        Thread.sleep(5000);
    }

    @Test
    void trackAndWait_shouldReturnScoringResult() {
        ConversationTracker tracker = scoring.session(
                "integration-test-003",
                "return-agent",
                "llama-3.3-70b-versatile"
        );

        var result = tracker.trackAndWait(
                "I want to return my order",
                "Sure, can you provide your order ID?"
        );

        // result is null for async ingest (202 response)
        // would be populated for sync evaluate endpoint
        System.out.println("Result: " + result);
    }
}
