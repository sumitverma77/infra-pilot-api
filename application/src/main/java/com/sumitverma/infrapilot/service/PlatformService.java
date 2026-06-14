package com.sumitverma.infrapilot.service;

import com.sumitverma.infrapilot.dto.CacheTestResponse;
import com.sumitverma.infrapilot.dto.DbTestResponse;
import com.sumitverma.infrapilot.dto.HealthResponse;
import com.sumitverma.infrapilot.dto.InfoResponse;
import com.sumitverma.infrapilot.dto.VersionResponse;

public interface PlatformService {

    HealthResponse health();

    VersionResponse version();

    InfoResponse info();

    CacheTestResponse cacheTest();

    DbTestResponse dbTest();
}