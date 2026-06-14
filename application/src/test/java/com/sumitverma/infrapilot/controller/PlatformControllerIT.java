package com.sumitverma.infrapilot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sumitverma.infrapilot.dto.CacheTestResponse;
import com.sumitverma.infrapilot.dto.DbTestResponse;
import com.sumitverma.infrapilot.dto.HealthResponse;
import com.sumitverma.infrapilot.dto.InfoResponse;
import com.sumitverma.infrapilot.dto.VersionResponse;
import com.sumitverma.infrapilot.service.PlatformService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "infrapilot.app-name=InfraPilot",
        "infrapilot.app-version=1.0.0-test",
        "infrapilot.git-commit-sha=abc123def",
        "infrapilot.build-timestamp=2026-06-14T00:00:00Z",
        "infrapilot.hostname=test-host",
        "infrapilot.environment=test",
        "spring.datasource.url=jdbc:h2:mem:infrapilot;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
@AutoConfigureMockMvc
class PlatformControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlatformService platformService;

    @Test
    void versionEndpointReturnsVersionMetadata() throws Exception {
        given(platformService.version()).willReturn(new VersionResponse("InfraPilot", "1.0.0-test", "abc123def", "2026-06-14T00:00:00Z"));

        mockMvc.perform(get("/api/v1/version"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.applicationName").value("InfraPilot"))
                .andExpect(jsonPath("$.applicationVersion").value("1.0.0-test"))
                .andExpect(jsonPath("$.gitCommitSha").value("abc123def"))
                .andExpect(jsonPath("$.buildTimestamp").value("2026-06-14T00:00:00Z"));
    }

    @Test
    void infoEndpointReturnsRuntimeMetadata() throws Exception {
        given(platformService.info()).willReturn(new InfoResponse("test-host", "test", List.of("test"), "21"));

        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hostname").value("test-host"))
                .andExpect(jsonPath("$.environment").value("test"))
                .andExpect(jsonPath("$.activeProfiles[0]").value("test"))
                .andExpect(jsonPath("$.javaVersion").value("21"));
    }

    @Test
    void healthEndpointReturnsActuatorAwarePayload() throws Exception {
        given(platformService.health()).willReturn(new HealthResponse("UP", "ACCEPTING_TRAFFIC", "CORRECT", Map.of("database", "UP")));

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.readinessState").value("ACCEPTING_TRAFFIC"))
                .andExpect(jsonPath("$.livenessState").value("CORRECT"))
                .andExpect(jsonPath("$.details.database").value("UP"));
    }
}
