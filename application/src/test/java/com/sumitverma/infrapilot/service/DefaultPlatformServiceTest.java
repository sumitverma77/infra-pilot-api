package com.sumitverma.infrapilot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sumitverma.infrapilot.config.ApplicationMetadataProperties;
import com.sumitverma.infrapilot.entity.SystemEvent;
import com.sumitverma.infrapilot.repository.SystemEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class DefaultPlatformServiceTest {

    @Test
    void cacheTestWritesAndReadsTimestamp() {
        ApplicationMetadataProperties properties = buildProperties();
        ApplicationAvailability availability = mock(ApplicationAvailability.class);
        HealthEndpoint healthEndpoint = mock(HealthEndpoint.class);
        Environment environment = mock(Environment.class);
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        SystemEventRepository systemEventRepository = mock(SystemEventRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-06-14T00:00:00Z"), ZoneOffset.UTC);

        given(availability.getReadinessState()).willReturn(ReadinessState.ACCEPTING_TRAFFIC);
        given(availability.getLivenessState()).willReturn(LivenessState.CORRECT);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("infrapilot:cache:test:timestamp")).willReturn("2026-06-14T00:00:00Z");

        DefaultPlatformService service = new DefaultPlatformService(
                properties,
                availability,
                healthEndpoint,
                environment,
                redisTemplate,
                systemEventRepository,
                clock);

        var response = service.cacheTest();

        assertThat(response.cacheKey()).isEqualTo("infrapilot:cache:test:timestamp");
        assertThat(response.writtenAt()).isEqualTo("2026-06-14T00:00:00Z");
        assertThat(response.readAt()).isEqualTo("2026-06-14T00:00:00Z");
        assertThat(response.cacheHit()).isTrue();
    }

    @Test
    void dbTestPersistsAndReadsBackSystemEvent() {
        ApplicationMetadataProperties properties = buildProperties();
        ApplicationAvailability availability = mock(ApplicationAvailability.class);
        HealthEndpoint healthEndpoint = mock(HealthEndpoint.class);
        Environment environment = mock(Environment.class);
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        SystemEventRepository systemEventRepository = mock(SystemEventRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-06-14T00:00:00Z"), ZoneOffset.UTC);

        given(availability.getReadinessState()).willReturn(ReadinessState.ACCEPTING_TRAFFIC);
        given(availability.getLivenessState()).willReturn(LivenessState.CORRECT);

        SystemEvent saved = new SystemEvent("DB_TEST", "{\"message\":\"infra-pilot-db-test\"}");
        setId(saved, 101L);
        setCreatedAt(saved, Instant.parse("2026-06-14T00:00:00Z"));

        given(systemEventRepository.save(any(SystemEvent.class))).willReturn(saved);
        given(systemEventRepository.findById(101L)).willReturn(Optional.of(saved));

        DefaultPlatformService service = new DefaultPlatformService(
                properties,
                availability,
                healthEndpoint,
                environment,
                redisTemplate,
                systemEventRepository,
                clock);

        var response = service.dbTest();

        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.eventType()).isEqualTo("DB_TEST");
        assertThat(response.status()).isEqualTo("SUCCESS");
    }

    private ApplicationMetadataProperties buildProperties() {
        ApplicationMetadataProperties properties = new ApplicationMetadataProperties();
        properties.setAppName("InfraPilot");
        properties.setAppVersion("1.0.0-test");
        properties.setGitCommitSha("abc123def");
        properties.setBuildTimestamp("2026-06-14T00:00:00Z");
        properties.setHostname("test-host");
        properties.setEnvironment("test");
        return properties;
    }

    private static void setId(SystemEvent event, Long id) {
        try {
            var field = SystemEvent.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(event, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void setCreatedAt(SystemEvent event, Instant createdAt) {
        try {
            var field = SystemEvent.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(event, createdAt);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
