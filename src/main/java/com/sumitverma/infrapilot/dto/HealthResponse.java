package com.sumitverma.infrapilot.dto;

import java.util.Map;

public record HealthResponse(
        String status,
        String readinessState,
        String livenessState,
        Map<String, Object> details) {
}