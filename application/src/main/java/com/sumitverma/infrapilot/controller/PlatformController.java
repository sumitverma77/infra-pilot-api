package com.sumitverma.infrapilot.controller;

import com.sumitverma.infrapilot.dto.CacheTestResponse;
import com.sumitverma.infrapilot.dto.DbTestResponse;
import com.sumitverma.infrapilot.dto.HealthResponse;
import com.sumitverma.infrapilot.dto.InfoResponse;
import com.sumitverma.infrapilot.dto.VersionResponse;
import com.sumitverma.infrapilot.service.PlatformService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PlatformController {

    private final PlatformService platformService;

    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(platformService.health());
    }

    @GetMapping("/version")
    public ResponseEntity<VersionResponse> version() {
        return ResponseEntity.ok(platformService.version());
    }

    @GetMapping("/info")
    public ResponseEntity<InfoResponse> info() {
        return ResponseEntity.ok(platformService.info());
    }

    @GetMapping("/cache-test")
    public ResponseEntity<CacheTestResponse> cacheTest() {
        return ResponseEntity.ok(platformService.cacheTest());
    }

    @GetMapping("/db-test")
    public ResponseEntity<DbTestResponse> dbTest() {
        return ResponseEntity.ok(platformService.dbTest());
    }
}