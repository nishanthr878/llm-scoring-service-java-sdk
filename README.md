# LLM Scoring Service — Java SDK

Java SDK for [LLM Scoring Service](https://github.com/nishanthr878/llm-scoring-service).

## Install

```xml
<dependency>
  <groupId>com.llmscoring</groupId>
  <artifactId>llm-scoring-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Quick Start

```java
// Configure once at startup
LLMScoring scoring = LLMScoring.create(
    LLMScoringConfig.builder()
        .scoringUrl("http://localhost:8080")
        .defaultScenario("return-agent")
        .mode(TrackingMode.SLIDING_WINDOW)
        .windowSize(5)
        .build()
);

// Track after every bot response — non-blocking
ConversationTracker tracker = scoring.session(sessionId);
tracker.track(userMessage, botResponse);
```

## Tracking Modes

| Mode | Behavior |
|------|----------|
| `TURN_BY_TURN` | Sends only the latest turn |
| `SLIDING_WINDOW` | Sends last N turns (default, recommended) |
| `FULL_CONVERSATION` | Sends entire history |

## With Callback

```java
scoring.session(sessionId)
    .onComplete(result -> {
        if (!result.isOverallPassed()) {
            alertService.notify(sessionId, result.getFlagReasons());
        }
    });
```

## Synchronous (for tests)

```java
ScoringResult result = tracker.trackAndWait(userMessage, botResponse);
assertThat(result.isOverallPassed()).isTrue();
```

## Requirements

Java 21+
