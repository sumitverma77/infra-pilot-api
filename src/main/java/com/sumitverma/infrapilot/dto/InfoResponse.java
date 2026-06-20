package com.sumitverma.infrapilot.dto;

import java.util.List;

public record InfoResponse(
        String hostname,
        String environment,
        List<String> activeProfiles,
        String javaVersion) {
}