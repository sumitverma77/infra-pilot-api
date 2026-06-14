package com.sumitverma.infrapilot.repository;

import com.sumitverma.infrapilot.entity.SystemEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemEventRepository extends JpaRepository<SystemEvent, Long> {
}