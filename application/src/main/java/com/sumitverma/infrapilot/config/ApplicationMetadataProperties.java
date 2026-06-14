package com.sumitverma.infrapilot.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "infrapilot")
public class ApplicationMetadataProperties {

    @NotBlank
    private String appName;

    @NotBlank
    private String appVersion;

    @NotBlank
    private String gitCommitSha;

    @NotBlank
    private String buildTimestamp;

    @NotBlank
    private String hostname;

    @NotBlank
    private String environment;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getGitCommitSha() {
        return gitCommitSha;
    }

    public void setGitCommitSha(String gitCommitSha) {
        this.gitCommitSha = gitCommitSha;
    }

    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    public void setBuildTimestamp(String buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}