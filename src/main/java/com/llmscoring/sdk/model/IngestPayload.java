package com.llmscoring.sdk.model;

import lombok.Data;

import java.util.List;

@Data
public class IngestPayload {
    private String sessionId;
    private String scenarioName;
    private String format;
    private String modelName;
    private String promptVersion;
    private List<ChatMessage> messages;
}
