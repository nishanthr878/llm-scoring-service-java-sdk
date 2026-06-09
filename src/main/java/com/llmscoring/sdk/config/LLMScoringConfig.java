package com.llmscoring.sdk.config;

import com.llmscoring.sdk.model.TrackingMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LLMScoringConfig {

    // URL of the scoring service
    private String scoringUrl;

    // Default scenario — can be overridden per track() call
    private String defaultScenario;

    // Tracking mode
    @Builder.Default
    private TrackingMode mode = TrackingMode.SLIDING_WINDOW;

    // Window size for SLIDING_WINDOW mode
    @Builder.Default
    private int windowSize = 5;

    // HTTP timeout in seconds
    @Builder.Default
    private int timeoutSeconds = 10;

    // Max retries on failure
    @Builder.Default
    private int maxRetries = 3;

    // If true — swallow errors silently (recommended for production)
    // If false — throw exceptions on failure
    @Builder.Default
    private boolean silentOnError = true;
}
