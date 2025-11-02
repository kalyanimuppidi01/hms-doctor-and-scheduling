package org.hms.doctor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/healthcheck")
public class HealthCheckController {

    // --- Liveness probe ---
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> live() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "timestamp", OffsetDateTime.now().toString(),
                        "service", "doctor-service"
                )
        );
    }

    // --- Readiness probe ---
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        try {
            // optionally check DB or cache connectivity
            // e.g. run a lightweight query if you want
            return ResponseEntity.ok(
                    Map.of(
                            "status", "READY",
                            "timestamp", OffsetDateTime.now().toString(),
                            "service", "doctor-service"
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(503).body(
                    Map.of(
                            "status", "NOT_READY",
                            "timestamp", OffsetDateTime.now().toString(),
                            "service", "doctor-service",
                            "error", e.getMessage()
                    )
            );
        }
    }
}
