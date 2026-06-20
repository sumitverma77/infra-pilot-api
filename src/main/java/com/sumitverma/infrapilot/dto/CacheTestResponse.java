package com.sumitverma.infrapilot.dto;

public record CacheTestResponse(
        String cacheKey,
        String writtenAt,
        String readAt,
        boolean cacheHit) {
}