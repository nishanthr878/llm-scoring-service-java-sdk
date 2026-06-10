package com.llmscoring.sdk.tracker;

import com.llmscoring.sdk.config.LLMScoringConfig;
import com.llmscoring.sdk.http.ScoringHttpClient;
import com.llmscoring.sdk.model.ChatMessage;
import com.llmscoring.sdk.model.IngestPayload;
import com.llmscoring.sdk.model.ScoringResponse;
import com.llmscoring.sdk.model.TrackingMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ConversationTracker {

    private final LLMScoringConfig config;
    private final ScoringHttpClient httpClient;
    private final String sessionId;
    private final String scenarioName;
    private final String modelName;
    private final List<ChatMessage> history;
    private Consumer<ScoringResponse> onCompleteCallback;

    public ConversationTracker(LLMScoringConfig config,
                        ScoringHttpClient httpClient,
                        String sessionId,
                        String scenarioName,
                        String modelName) {
        this.config = config;
        this.httpClient = httpClient;
        this.sessionId = sessionId;
        this.scenarioName = scenarioName;
        this.modelName = modelName;
        this.history = new ArrayList<>();
    }

    // Register callback for when scoring completes
    public ConversationTracker onComplete(Consumer<ScoringResponse> callback) {
        this.onCompleteCallback = callback;
        return this;
    }

    // Add a turn — called after every user+assistant exchange
    public void track(String userMessage, String assistantResponse) {
        history.add(ChatMessage.user(userMessage));
        history.add(ChatMessage.assistant(assistantResponse));

        List<ChatMessage> messagesToSend = resolveMessages();
        IngestPayload payload = buildPayload(messagesToSend);

        submitAsync(payload);
    }

    // Synchronous version — waits for scoring result
    // Useful for test suites
    public ScoringResponse trackAndWait(String userMessage,
                                         String assistantResponse) {
        history.add(ChatMessage.user(userMessage));
        history.add(ChatMessage.assistant(assistantResponse));

        List<ChatMessage> messagesToSend = resolveMessages();
        IngestPayload payload = buildPayload(messagesToSend);

        return httpClient.ingestSync(payload);
    }

    // Submit full conversation manually
    public void submitAll() {
        IngestPayload payload = buildPayload(new ArrayList<>(history));
        submitAsync(payload);
    }

    private List<ChatMessage> resolveMessages() {
        return switch (config.getMode()) {
            case TURN_BY_TURN -> {
                // Only last user + assistant turn
                int size = history.size();
                yield history.subList(Math.max(0, size - 2), size);
            }
            case SLIDING_WINDOW -> {
                // Last N messages (windowSize * 2 because each turn = 2 messages)
                int windowMessages = config.getWindowSize() * 2;
                int size = history.size();
                yield history.subList(Math.max(0, size - windowMessages), size);
            }
            case FULL_CONVERSATION -> new ArrayList<>(history);
        };
    }

    private IngestPayload buildPayload(List<ChatMessage> messages) {
        IngestPayload payload = new IngestPayload();
        payload.setSessionId(sessionId);
        payload.setScenarioName(scenarioName);
        payload.setModelName(modelName);
        payload.setFormat("openai");
        payload.setMessages(messages);
        return payload;
    }

    private void submitAsync(IngestPayload payload) {
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return httpClient.ingestSync(payload);
                    } catch (Exception e) {
                        handleError(e);
                        return null;
                    }
                })
                .thenAccept(result -> {
                    if (result != null && onCompleteCallback != null) {
                        onCompleteCallback.accept(result);
                    }
                })
                .exceptionally(e -> {
                    handleError(e);
                    return null;
                });
    }


    private void handleError(Throwable e) {
        if (config.isSilentOnError()) {
            System.err.println("[LLMScoring SDK] Error: " + e.getMessage());
        } else {
            throw new RuntimeException("[LLMScoring SDK] Failed to submit", e);
        }
    }
}
