package com.sumitverma.infrapilot.dto;

import java.time.Instant;

public record DbTestResponse(
        Long id,
        String eventType,
        String payload,
        Instant createdAt,
        String status) {
}