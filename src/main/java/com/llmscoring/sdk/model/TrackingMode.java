package com.llmscoring.sdk.model;

public enum TrackingMode {
    TURN_BY_TURN,       // send each turn independently
    SLIDING_WINDOW,     // send last N turns (default)
    FULL_CONVERSATION   // send entire conversation history
}
