package com.sumitverma.infrapilot.dto;

public record VersionResponse(
        String applicationName,
        String applicationVersion,
        String gitCommitSha,
        String buildTimestamp) {
}