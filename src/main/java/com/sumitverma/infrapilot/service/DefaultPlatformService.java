package com.sumitverma.infrapilot.service;

import com.sumitverma.infrapilot.config.ApplicationMetadataProperties;
import com.sumitverma.infrapilot.dto.CacheTestResponse;
import com.sumitverma.infrapilot.dto.DbTestResponse;
import com.sumitverma.infrapilot.dto.HealthResponse;
import com.sumitverma.infrapilot.dto.InfoResponse;
import com.sumitverma.infrapilot.dto.VersionResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

@Service
public class DefaultPlatformService implements PlatformService {

    private static final Logger log = LoggerFactory.getLogger(DefaultPlatformService.class);

    private final ApplicationMetadataProperties applicationMetadataProperties;
    private final ApplicationAvailability applicationAvailability;
    private final HealthEndpoint healthEndpoint;
    private final Environment environment;
    private final Clock clock;

    public DefaultPlatformService(
            ApplicationMetadataProperties applicationMetadataProperties,
            ApplicationAvailability applicationAvailability,
            HealthEndpoint healthEndpoint,
            Environment environment,
            Clock clock) {
        this.applicationMetadataProperties = applicationMetadataProperties;
        this.applicationAvailability = applicationAvailability;
        this.healthEndpoint = healthEndpoint;
        this.environment = environment;
        this.clock = clock;
    }

    @Override
    public HealthResponse health() {
        String actuatorHealthSnapshot = healthEndpoint.health().toString();
        String status = applicationAvailability.getReadinessState() == ReadinessState.ACCEPTING_TRAFFIC ? "UP" : "DOWN";
        return new HealthResponse(
            status,
                applicationAvailability.getReadinessState().name(),
                applicationAvailability.getLivenessState().name(),
            new LinkedHashMap<>(Map.of("actuatorHealthSnapshot", actuatorHealthSnapshot)));
    }

    @Override
    public VersionResponse version() {
        return new VersionResponse(
                applicationMetadataProperties.getAppName(),
                applicationMetadataProperties.getAppVersion(),
                applicationMetadataProperties.getGitCommitSha(),
                applicationMetadataProperties.getBuildTimestamp());
    }

    @Override
    public InfoResponse info() {
        return new InfoResponse(
                applicationMetadataProperties.getHostname(),
                applicationMetadataProperties.getEnvironment(),
                Arrays.asList(environment.getActiveProfiles()),
                System.getProperty("java.version"));
    }

    @Override
    public CacheTestResponse cacheTest() {
        return new CacheTestResponse("disabled", "disabled", "disabled", false);
    }

    @Override
    public DbTestResponse dbTest() {
        return new DbTestResponse(0L, "DB_TEST", "disabled", Instant.now(), "DISABLED");
    }
}