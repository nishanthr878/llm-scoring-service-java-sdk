package com.llmscoring.sdk;

import com.llmscoring.sdk.config.LLMScoringConfig;
import com.llmscoring.sdk.http.ScoringHttpClient;
import com.llmscoring.sdk.tracker.ConversationTracker;

public class LLMScoring {

    private final LLMScoringConfig config;
    private final ScoringHttpClient httpClient;

    private LLMScoring(LLMScoringConfig config) {
        this.config = config;
        this.httpClient = new ScoringHttpClient(
                config.getScoringUrl(),
                config.getTimeoutSeconds(),
                config.getMaxRetries()
        );
    }

    // Entry point — create from config
    public static LLMScoring create(LLMScoringConfig config) {
        return new LLMScoring(config);
    }

    // Create a tracker for a specific session
    public ConversationTracker session(String sessionId) {
        return new ConversationTracker(
                config,
                httpClient,
                sessionId,
                config.getDefaultScenario(),
                null
        );
    }

    // Create a tracker with scenario override
    public ConversationTracker session(String sessionId, String scenarioName) {
        return new ConversationTracker(
                config,
                httpClient,
                sessionId,
                scenarioName,
                null
        );
    }

    // Create a tracker with full options
    public ConversationTracker session(String sessionId,
                                       String scenarioName,
                                       String modelName) {
        return new ConversationTracker(
                config,
                httpClient,
                sessionId,
                scenarioName,
                modelName
        );
    }
}
