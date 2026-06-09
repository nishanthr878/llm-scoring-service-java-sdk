package com.llmscoring.sdk.model;

import lombok.Data;

import java.util.Map;

@Data
public class ScoringResponse {
    private Long id;
    private String sessionId;
    private String type;
    private Map<String, Double> scores;
    private Map<String, String> reasoning;
    private Map<String, Boolean> passed;
    private Boolean overallPassed;
    private String flagReasons;
    private String scoredAt;
}
