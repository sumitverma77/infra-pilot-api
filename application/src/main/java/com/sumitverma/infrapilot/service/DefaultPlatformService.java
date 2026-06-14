package com.sumitverma.infrapilot.service;

import com.sumitverma.infrapilot.config.ApplicationMetadataProperties;
import com.sumitverma.infrapilot.dto.CacheTestResponse;
import com.sumitverma.infrapilot.dto.DbTestResponse;
import com.sumitverma.infrapilot.dto.HealthResponse;
import com.sumitverma.infrapilot.dto.InfoResponse;
import com.sumitverma.infrapilot.dto.VersionResponse;
import com.sumitverma.infrapilot.entity.SystemEvent;
import com.sumitverma.infrapilot.repository.SystemEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.env.Environment;

@Service
public class DefaultPlatformService implements PlatformService {

    private static final Logger log = LoggerFactory.getLogger(DefaultPlatformService.class);
    private static final String CACHE_KEY = "infrapilot:cache:test:timestamp";

    private final ApplicationMetadataProperties applicationMetadataProperties;
    private final ApplicationAvailability applicationAvailability;
    private final HealthEndpoint healthEndpoint;
    private final Environment environment;
    private final RedisTemplate<String, String> redisTemplate;
    private final SystemEventRepository systemEventRepository;
    private final Clock clock;

    public DefaultPlatformService(
            ApplicationMetadataProperties applicationMetadataProperties,
            ApplicationAvailability applicationAvailability,
            HealthEndpoint healthEndpoint,
            Environment environment,
            RedisTemplate<String, String> redisTemplate,
            SystemEventRepository systemEventRepository,
            Clock clock) {
        this.applicationMetadataProperties = applicationMetadataProperties;
        this.applicationAvailability = applicationAvailability;
        this.healthEndpoint = healthEndpoint;
        this.environment = environment;
        this.redisTemplate = redisTemplate;
        this.systemEventRepository = systemEventRepository;
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
        String writtenAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now(clock));
        log.info("Writing cache test payload at {}", writtenAt);
        redisTemplate.opsForValue().set(CACHE_KEY, writtenAt);
        String readAt = redisTemplate.opsForValue().get(CACHE_KEY);
        return new CacheTestResponse(CACHE_KEY, writtenAt, readAt, readAt != null && readAt.equals(writtenAt));
    }

    @Override
    @Transactional
    public DbTestResponse dbTest() {
        String payload = "{\"message\":\"infra-pilot-db-test\"}";
        SystemEvent savedEvent = systemEventRepository.save(new SystemEvent("DB_TEST", payload));
        SystemEvent fetchedEvent = systemEventRepository.findById(savedEvent.getId())
                .orElseThrow(() -> new DataAccessException("Persisted system event was not found") {
                });

        return new DbTestResponse(
                fetchedEvent.getId(),
                fetchedEvent.getEventType(),
                fetchedEvent.getPayload(),
                fetchedEvent.getCreatedAt(),
                "SUCCESS");
    }
}