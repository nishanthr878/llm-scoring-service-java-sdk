# LLM Scoring Service — Java SDK

Java SDK for [LLM Scoring Service](https://github.com/nishanthr878/llm-scoring-service).

## Install

**Option 1 — From source (recommended for now)**

```bash
git clone https://github.com/nishanthr878/llm-scoring-service-java-sdk
cd llm-scoring-service-java-sdk
mvn install -DskipTests
```

Then add to your `pom.xml`:

```xml
<dependency>
  <groupId>com.llmscoring</groupId>
  <artifactId>llm-scoring-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Option 2 — GitHub Packages**

Add repository to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/nishanthr878/llm-scoring-service-java-sdk</url>
  </repository>
</repositories>

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
        .silentOnError(true)
        .build()
);

// Create a tracker per session
ConversationTracker tracker = scoring.session(sessionId);

// Call after every bot response — non-blocking, fire and forget
tracker.track(userMessage, botResponse);
```

## Tracking Modes

| Mode | Behavior | Best for |
|------|----------|----------|
| `TURN_BY_TURN` | Sends only the latest turn | Low latency, simple bots |
| `SLIDING_WINDOW` | Sends last N turns (default) | Most use cases |
| `FULL_CONVERSATION` | Sends entire history | Short conversations |

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

## Scenario Override

```java
// Override default scenario per session
ConversationTracker tracker = scoring.session(
    sessionId,
    "support-agent",  // scenario name
    "gpt-4"           // model name
);
```

## Configuration Reference

| Option | Default | Description |
|--------|---------|-------------|
| `scoringUrl` | required | URL of the scoring service |
| `defaultScenario` | null | Default scenario name |
| `mode` | `SLIDING_WINDOW` | Tracking mode |
| `windowSize` | 5 | Turns in sliding window |
| `timeoutSeconds` | 10 | HTTP timeout |
| `maxRetries` | 3 | Retry attempts on failure |
| `silentOnError` | true | Swallow errors in production |

## Requirements

- Java 21+
- LLM Scoring Service running and accessible

## Related

- [LLM Scoring Service](https://github.com/nishanthr878/llm-scoring-service)
- [Python SDK](https://github.com/nishanthr878/llm-scoring-service-python-sdk)
- [UI](https://github.com/nishanthr878/llm-scoring-service-ui)

## License

MIT
